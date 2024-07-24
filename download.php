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
$baseUrl="{$scheme}://{$host}";

if(($scheme==="http"&&$port!=="80")||($scheme==="https"&&$port!=="443")) 
{
    $baseUrl.=":{$port}";
}

?>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
<?php

$uuid=htmlspecialchars(trim($_GET["uuid"]??""));
if(!preg_match("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[4][0-9a-fA-F]{3}-[89ABab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/",$uuid))
{
    ?>
        <div class="alert alert-danger" role="alert">Sorry, something went wrong.</div>
    <?php
    exit;
}

$entry=DatabaseTools::getEntry($uuid);
if(!$entry) 
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
            <div class="alert alert-warning" role="alert">The requested file does not exist anymore.</div>'
        <?php
        exit;
    }
}
else
{
    $probePacket=json_decode($entry["ffprobe"]);
    $thumbUrl=$baseUrl."/thumbnails/".$entry["uuid"].".jpg";
    $thumbnail=base64_encode(file_get_contents($thumbUrl));

    ?>
        <div class="text-center bg-dark text-white" style="padding: 20px;">
            <img src="data:image/jpeg;base64, <?php echo $thumbnail; ?>" alt="Thumbnail" class="img-thumbnail">
            <div class="text-center bg-dark text-white">
                <?php
                echo "<p><small>
                    Format: <b>{$probePacket->format_long_name}</b>, Release date: <b>"
                    .convertDate($probePacket->tags->date)
                    ."</b>, Duration: <b>"
                    .convertDuration($probePacket->duration)."</b></small></p>";
                echo "<h3><a href='{$probePacket->tags->comment}' class='link-info link-offset-2 link-underline-opacity-25 link-underline-opacity-100-hover'>{$probePacket->tags->title}</a></h3>";
                echo "<h4>{$probePacket->tags->artist}</h4>";
                echo "<p>{$probePacket->tags->description}</p>";
                ?>
            </div>
            <form method="POST">
                <button type="submit" name="download" class="btn btn-primary">Download File [<?php
                echo convertBytesToMB($probePacket->size);
                ?> MB]</button>
            </form>
        </div>
    <?php
}

function convertBytesToMB($bytes)
{
    if(!is_numeric($bytes)) return $bytes;
    return round($bytes/1048576,2);
}

function convertDuration($duration) 
{
    if(!is_numeric($duration)) return $duration;
    $h=floor($duration/3600);
    $m=floor(($duration%3600)/60);
    $s=$duration%60;
    return sprintf('%02d:%02d:%02d',$h,$m,$s);
}

function convertDate($string)
{
    $date=DateTime::createFromFormat('Ymd',$string);
    if($date&&$date->format("Ymd")===$string)
    {
        return $date->format("d.m.Y");
    }
    return $string;
}

?>
</body>
</html>