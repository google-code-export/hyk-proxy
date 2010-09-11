<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
$encKey = 1;

function debug($msg)
{
    //echo $msg;
    //flush();
   // ob_flush();
}

function decode($input) {
    global $encKey;
    $line = "";
    $len = strlen($input);
    for ($i = 0; $i < $len; $i++) {
        $line .= chr(ord($input[$i])+1);
    }
    return $line;
}

function encode($input) {
    global $encKey;
    $line = "";
     $len = strlen($input);
    for ($i = 0; $i < $len; $i++) {
        $line .= chr(ord($input[$i])-1);
    }
    return $line;
}

error_reporting(0);
$clientip = $_SERVER["REMOTE_ADDR"];
$headers = getallheaders();

$tunnelTarget = decode(trim($headers["TunnelTarget"]));
//echo "target is ".$tunnelTarget;
$tunnelSessionId = $headers["TunnelSessionId"];


$pos = strpos($tunnelTarget, ":");
$targetHost = trim(substr($tunnelTarget, 0, $pos));
$targetport = intval(trim(substr($tunnelTarget, $pos+1)));
//echo "start here".$targetHost.$targetport;

$rs = fsockopen($targetHost, $targetport);

//$rs = fsockopen("217.69.173.120", 80);
if($rs == null)
{
     header('HTTP/1.0 500 Internal Server Error');
     die();
}
//http only
if(null == $tunnelSessionId)
{
    $input = fopen('php://input', 'r');
    $output = fopen('php://output', 'w');
    //echo "starthere!";

    while(!feof($input))
    {
        $buffer = fread($input, 4096);
        if($buffer != null)
        {
            $buffer = decode($buffer);
            fwrite($rs, $buffer);
        }
    }
   //debug( "Finish input!");
   //fwrite($rs, "GET http://www.google.com HTTP/1.0\r\n\r\n");
    while(!feof($rs))
    {
        $buffer = fread($rs, 4096);
        if($buffer != null)
        {
            fwrite($output, encode($buffer));
        }
    }
    exit();
}

$tunnelSrc = $headers["TunnelSource"];
if($tunnelSrc == null)
{
    header('HTTP/1.0 500 Internal Server Error');
    die();
}
$pos = strpos($tunnelSrc, ":");
$clientport = intval(substr($tunnelSrc, $pos+1));
$rc = fsockopen($clientip, $clientport);
if($rc == null)
{
     header('HTTP/1.0 500 Internal Server Error');
     die();
}
//fwrite($rc, "before set non block");
$ret = stream_set_blocking($rc, 0);
if($ret == false)
{
    debug("Failed to set non block for local!");
    header('HTTP/1.0 500 Internal Server Error');
    die();
}
$ret = stream_set_blocking($rs, 0);
if($ret == false)
{
    debug("Failed to set non block fore remote!");
    header('HTTP/1.0 500 Internal Server Error');
    die();
}
$timeout = 30;
$convenient_read_block = 10240;
$sockets = array($rc, $rs);
header("Content-Type: text/plain");
// fwrite($rc, "before start here");

fwrite($rc, trim($tunnelSessionId));
while (count($sockets)) {
    $read = $sockets;
    if(feof($rc) || feof($rs))
    {
        break;
    }
    $ret = stream_select($read, $w = null, $e = null, $timeout);
    //debug("select result:".$ret);
    if (count($read)) {
        /* stream_select generally shuffles $read, so we need to
          compute from which socket(s) we're reading. */
        foreach ($read as $r) {
            $id = array_search($r, $sockets);
            $data = fread($r, $convenient_read_block);
            /* A socket is readable either because it has
              data to read, OR because it's at EOF. */
            if (strlen($data) == 0) {
                 unset($id);
                 break;
            } else {
                if ($id == 0) {
                    $data = decode($data);
                    //echo "send request:".$data;
                    fwrite($rs, $data);
                } else {
                    $data = encode($data);
                    fwrite($rc, $data);
                    //fwrite($rc, encode($data));
                }
            }
        }
    } else {
        /* A time-out means that *all* streams have failed
          to receive a response. */
        //fwrite($rc, "Select failed".$id);
        break;
    }
}
fclose($rc);
fclose($rs);
echo "https connection finished!";
exit();
?>
