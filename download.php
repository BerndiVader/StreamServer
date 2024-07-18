<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Download Page</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body class="bg-dark text-white"></body>

<?php
include_once("Classes.php");
include_once("Config.php");

$scheme=isset($_SERVER["HTTPS"])&&$_SERVER["HTTPS"]!=="off"?"https":"http";
$host=$_SERVER["HTTP_HOST"];
$port=$_SERVER["SERVER_PORT"];
$baseUrl=$scheme."://".$host;

if(($scheme==="http"&&$port!=="80")||($scheme==="https"&&$port!=="443")) 
{
    $baseUrl.=":".$port;
}

?>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
<?php

$uuid = htmlspecialchars(trim($_GET["uuid"] ?? ''));
if (!preg_match('/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[4][0-9a-fA-F]{3}-[89ABab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/', $uuid))
{
    ?>
        <div class="alert alert-danger" role="alert">Sorry, something went wrong.</div>
    <?php
    exit;
}

$entry = DatabaseTools::getEntry($uuid);

if (!$entry) 
{
    ?>
        <div class="alert alert-warning" role="alert">The requested download does not exist, or the link is expired.</div>
    <?php
    exit;
}

if ($_SERVER["REQUEST_METHOD"]==="POST"&&isset($_POST["download"])) 
{
    try 
    {
        $download=new FileDownload($entry["path"]);
        $download->sendDownload();
    }
    catch (\InvalidArgumentException $e)
    {
        ?>
            <div class="alert alert-danger" role="alert">The requested file does not exist anymore.</div>'
        <?php
        exit;
    }
}
else
{
    $probePacket=json_decode($entry["ffprobe"]);
    if($probePacket)
    {
    }

    $thumbUrl=$baseUrl."/thumbnails/".$entry["uuid"].".jpg";

    ?>
        <div class="text-center bg-dark text-white" style="padding: 20px;">
            <img src="<?php echo $thumbUrl; ?>" alt="Thumbnail" style="max-width: 100%; height: auto; margin-bottom: 20px;">
            <div class="text-center bg-dark text-white">
                <?php
                echo "<h1>".$probePacket->tags->title."</h1>";
                echo "<p>".$probePacket->tags->comment."</p>";
                ?>
            </div>
            <form method="POST">
                <button type="submit" name="download" class="btn btn-primary">Download File</button>
            </form>
        </div>
    <?php
}

?>
</body>
</html>