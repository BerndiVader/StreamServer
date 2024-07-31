<?php
include_once("header.php");
include_once("Config.php");
include_once("Classes.php");

$code=isset($_GET["code"])?htmlspecialchars($_GET["code"]):"";
$uuid=isset($_GET["state"])?htmlspecialchars($_GET["state"]):"";
if(!preg_match("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[4][0-9a-fA-F]{3}-[89ABab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/",$uuid))
{
    error();
}

if($code)
{
    if(!DatabaseTools::addOauth2Verification($uuid,$code))
    {
        error();
    }
}
else
{
    error();
}

?>

<div class="container mt-5">
    <div class="alert alert-info" role="alert">
        Please enter the following code in your Bot console to complete the OAuth2 flow:
    </div>
    <div class="input-group mb-3">
        <input type="text" class="form-control" id="oauthCode" value="<?php echo $code; ?>" readonly>
        <div class="input-group-append">
            <button class="btn btn-outline-secondary" type="button" onclick="copyToClipboard()">Copy</button>
        </div>
    </div>
</div>

<!-- Include Bootstrap JS and dependencies -->
<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>

<script>
function copyToClipboard() {
    var copyText = document.getElementById("oauthCode");
    copyText.select();
    copyText.setSelectionRange(0, 99999); // For mobile devices
    document.execCommand("copy");
    alert("Code copied: " + copyText.value);
}
</script>

<?php

function error()
{
    ?>
    <div class="alert alert-danger" role="alert">
        An error occurred while processing your request.
    </div>
    <?php
    exit;
}

include_once("footer.php");
