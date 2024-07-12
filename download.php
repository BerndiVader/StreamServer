<?php
include_once("Classes.php");
include_once("Config.php");

$uuid = htmlspecialchars(trim($_GET["uuid"] ?? ''));
if(!preg_match('/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[4][0-9a-fA-F]{3}-[89ABab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/',$uuid)) 
{
    ?>
        Sorry, something went wrong.
    <?php    
    exit;
}

$entry=DatabaseTools::getEntry(htmlspecialchars($_GET["uuid"]));
if(!$entry)
{
    ?>
        The requested download does not exists, or the link is expired.
    <?php
    exit;    
}

try {
    $download=new FileDownload($entry["path"]);
    $download->sendDownload();
} catch (\InvalidArgumentException $e) {
    ?>
        The requested file does not exists anymore.
    <?php
    exit;
}
