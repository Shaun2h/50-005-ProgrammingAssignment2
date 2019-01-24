# 50-005 Programming Assignment 2
A basic Server/Client file sending mod. Practice for AES/RSA Encryption. <br>
Note: This is not a command line app, and changes must be done within the files itself.
### Requirements
Java. Any version will do.

### Control
sendWith_ServerPublicKeyEncrypted / send_file_with_AES  are the methods inside ClientWithSecurity to be changed.<br>
File name/location is also within the same files. Place your file name in place of rr.txt to be sent.<br>
This project is currently hard coded to only use Alice/Bob's private/public keys that are also available in this repository.<br>

#### Note
This project is a practice in basic encryption and should not under any circumstances be used for actual secure file transmission. This is especially due to lack of salt added to encryption and other various security flaws.<br>
This project serves as an example of using in built java libraries to send and receive files.<br>
