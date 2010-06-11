<?php
/* ----------------------------------------------------------------------------
 *
 * This PHP module provides methods to connect to and communicate with the
 * BaseX Server.
 *
 * The Constructor of the class expects a hostname, port, username and password
 * for the connection. The socket connection will then be established via the
 * hostname and the port.
 *
 * For the execution of commands you need to call the execute() method with the
 * database command as argument. The method returns the result or throws
 * an exception with the received error message.
 * For the execution of the iterative version of a query you need to call
 * the query() method. The results will then be returned via the more() and
 * the next() methods. If an error occurs an exception will be thrown.
 *
 * ----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * ----------------------------------------------------------------------------
 */
class Session {
  /* Class variables.*/
  var $socket, $info, $buffer, $bpos, $bsize;

  /* Constructor, creating a new socket connection. */
  function __construct($h, $p, $user, $pw) {
    // create server connection
    $this->socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if(!socket_connect($this->socket, $h, $p)) {
      throw new Exception("Can't communicate with server.");
    }

    // receive timestamp
    $ts = $this->readString();

    // send username and hashed password/timestamp
    $md5 = hash("md5", hash("md5", $pw).$ts);
    socket_write($this->socket, "$user\0$md5\0");

    // receives success flag
    if(socket_read($this->socket, 1) != "\0") {
      throw new Exception("Access denied.");
    }
  }

  /* Executes the specified command. */
  public function execute($com) {
    // send command to server
    socket_write($this->socket, "$com\0");

    // receive result
    $result = $this->receive();
    $this->info = $this->readString();
	if($this->ok() != True) {
	  throw new Exception($this->info);
	}
	return $result;
  }

  /* Returns processing information. */
  public function info() {
    return $this->info;
  }

  /* Closes the connection. */
  public function close() {
    socket_write($this->socket, "exit\0");
    socket_close($this->socket);
  }

  /* Initializes the byte transfer */
  private function init() {
    $this->bpos = 0;
    $this->bsize = 0;
  }

  /* Receives a string from the socket. */
  public function readString() {
    $com = "";
    while(($d = $this->read()) != "\0") {
      $com .= $d;
    }
    return $com;
  }

  /* Returns a single byte from the socket. */
  private function read() {
    if($this->bpos == $this->bsize) {
      $this->bsize = socket_recv($this->socket, $this->buffer, 4096, 0);
      $this->bpos = 0;
    }
    return $this->buffer[$this->bpos++];
  }
  
  /* Returns the query object.*/
  public function query($q) {
  	return new Query($this, $q);
  }
  
  /* Sends the str. */
  public function send($str) {
  	socket_write($this->socket, "$str\0");
  }
  
  /* Returns success check. */
  public function ok() {
  	return $this->read() == "\0";
  }
  
  /* Returns the result. */
  public function receive() {
  	$this->init();
  	return $this->readString();
  }
}

class Query {
  /* Class variables.*/
  var $session, $id, $open, $next;
 
  /* Constructor, creating a new query object. */	
  function __construct($s, $q) {
    $this->session = $s;
	$this->session->send("\0$q");
	$this->id = $this->session->receive();
	if($this->session->ok() != True) {
      throw new Exception($this->session->readString());
    }
  }
	
  /* Checks for next item in line. */
  public function more() {
    $this->session->send("\1$this->id");
    $this->next = $this->session->receive();
    if($this->session->ok() != True) {
      throw new Exception($this->session->readString());
    }
    return strlen($this->next) > 0; 
  }
	
  /* Returns next item. */
  public function next() {
    return $this->next;
  }
	
  /* Closes the query. */
  public function close() {
    $this->session->send("\2$this->id");   
  }	
}
?>