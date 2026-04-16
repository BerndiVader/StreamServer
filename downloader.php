<?php
include_once("header.php");

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

$uuid=htmlspecialchars(trim($_GET["uuid"]??""));
if(!preg_match("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[4][0-9a-fA-F]{3}-[89ABab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/",$uuid))
{
    ?>
    <div class="container py-4">
        <div class="row g-2">
            <div class="col">
                <input id="msg" type="text" placeholder="URL" class="form-control bg-dark text-light">
            </div>
            <div class="col-auto">
                <button class="btn btn-warning" id="sendBtn">Get</button>
                <span id="spinner" style="display:none;">
                    <span class="spinner-border spinner-border-sm text-warning"></span>
                </span>
            </div>
        </div>

        <div class="text-center my-4">
            <a id="downloadBtn" href="#" class="btn btn-success btn-lg" style="display:none;">Get Downloadlink</a>
        </div>

        <div id="bigSpinner" class="d-flex justify-content-center align-items-center"
            style="height:200px; display:none !important;">
            <div class="spinner-border text-light" style="width: 5rem; height: 5rem;" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
        </div>

        <div id="infoBox" class="container bg-dark text-light p-4 rounded" style="display:none;">
            <h2 id="title" class="mb-4"></h2>
            <div class="row">
                <div class="col-md-5 d-flex align-items-start justify-content-center">
                    <img id="thumbnail" class="img-fluid rounded" style="max-width:320px; max-height:320px;" />
                </div>
                <div class="col-md-7">
                    <p id="description" class="mb-2"></p>
                    <p><strong>Channel:</strong> <span id="channel"></span></p>
                    <p><strong>Uploader:</strong> <span id="uploader"></span></p>
                    <p><strong>Duration:</strong> <span id="duration"></span></p>
                </div>
            </div>
        </div>

        <textarea id="log" readonly class="container py-4 bg-dark text-light"
            style="height:120px;width:100%;overflow:auto;white-space:pre">
        </textarea>

    </div>

    <script src="websocket.js"></script>

    <?php
} 
else 
{
    $entry=DatabaseTools::getEntry($uuid);
    if(!$entry)
    {
        ?>
        <div class="alert alert-warning" role="alert">The requested download does not exist, or the link is expired.</div>
        <?php
        exit;
    }

    if($_SERVER["REQUEST_METHOD"]==="POST"&&isset($_POST["download"]))
    {
        try 
        {
            $download=new FileDownload($entry["path"]);
            $download->sendDownload();
        }
        catch(\InvalidArgumentException $e)
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

        <form method="POST">
            <div class="text-center my-4">
                <button type="submit" name="download" class="btn btn-primary btn-lg">Download File [<?php
                echo convertBytesToMB($probePacket->size);
                ?> MB]</button>
            </div>
        </form>

        <div class="text-center" style="padding: 20px;">
            <img src="data:image/jpeg;base64, <?php echo $thumbnail; ?>" alt="Thumbnail" class="img-thumbnail">
            <div class="card text-bg-dark shadow-sm w-100">
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
        </div>
        <?php
    }
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
    $m=floor(((int)$duration%3600)/60);
    $s=(int)$duration%60;
    return sprintf('%02d:%02d:%02d',$h,$m,$s);
}

function convertDate($string)
{
    $date=DateTime::createFromFormat('Ymd',$string);
    if($date&&$date->format("Ymd")===$string) return $date->format("d.m.Y");
    return $string;
}

include_once("footer.php");
