<?php
include_once("Classes.php");
include_once("Config.php");

if(!isset($_GET["uuid"])) 
{
    die();
}

$entry=DatabaseTools::getEntry(htmlspecialchars($_GET["uuid"]));
if(isset($entry)&&$entry!=false)
{
    $download=new FileDownload($entry["path"]);
    $download->sendDownload();
}
else
{
    ?>
    <b>The requested download does not exists, or the link is expired.</b>
    <?php
}
