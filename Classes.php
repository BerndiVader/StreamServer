<?php

class DatabaseTools
{    
    private function __construct() {}

    private static function getConnection()
    {
        $connection=new mysqli(Config::$host,Config::$user,Config::$pass,Config::$database,Config::$port);
        if($connection->connect_errno!=0)
        {
            error_log("Error getting connection: {$connection->connect_error}");
            throw new mysqli_sql_exception($connection->connect_error,$connection->connect_errno);
        }
        return $connection;
    }

    public static function getEntry($uuid)
    {
        $connection=DatabaseTools::getConnection();
        $statement=$connection->prepare("SELECT * FROM downloadables WHERE uuid=?");
        $statement->bind_param("s",$uuid);
        $statement->execute();
        $result=$statement->get_result();
        $connection->close();
        
        if($result->num_rows>0)
        {
            $array=$result->fetch_array(MYSQLI_ASSOC);
        }
        $result->close();
        return $array??null;
    }
}

class FileDownload
{
    private $path,$mime,$extension,$name,$size;

    public function __construct($filePath)
    {
        $filePath=realpath($filePath);
        if(!$filePath||!is_file($filePath)||!is_readable($filePath))
        {
            throw new \InvalidArgumentException("File does not exist or is not readable.");
        }

        $pointer=fopen($filePath,"rb");
        if(!$pointer||(!is_resource($pointer)&&!is_object($pointer)))
        {
            throw new \InvalidArgumentException("No valid file pointer.");
        }

        $this->path=$filePath;
        $this->name=basename($filePath);
        $this->extension=pathinfo($filePath,PATHINFO_EXTENSION);
        $this->size=$this->getFileSize($pointer);
        $this->mime=$this->getMimeType();
        fclose($pointer);
    }

    public function sendDownload($forceDownload=true)
    {
        if(headers_sent())
        {
            throw new \RuntimeException("Cannot send file to the browser, since the headers were already sent.");
        }

        header("Pragma: public");
        header("Expires: 0");
        header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
        header("Cache-Control: private",false);
        header("Content-Type: {$this->mime}");

        if($forceDownload)
        {
            header("Content-Disposition: attachment; filename=\"{$this->name}\";");
        }
        else
        {
            header("Content-Disposition: filename=\"{$this->name}\";");
        }

        header("Content-Transfer-Encoding: binary");
        header("Content-Length: {$this->size}");

        @ob_clean();

        $pointer=fopen($this->path,"rb");
        rewind($pointer);
        fpassthru($pointer);
        fclose($pointer);
    }

    private function getMimeType()
    {
        $info=new finfo(FILEINFO_MIME_TYPE);
        return $info->file($this->path)?:"application/force-download";
    }

    private function getFileSize($pointer)
    {
        $stat=fstat($pointer);
        return $stat['size'];
    }

    public function getSize()
    {
        return $this->size;
    }

    public function getName()
    {
        return $this->name;
    }

    public function getExtension()
    {
        return $this->extension;
    }

}