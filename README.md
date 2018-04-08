# 50-005 Programming Assignment 2
Simple Server-Client code with basic AES/RSA encryption, depending on what you prefer.<br>
control is within PoohClient / ServerPiglet.java. You just need to fill in the IP/file places.<br>


sendWith_ServerPublicKeyEncrypted / send_file_with_AES  are the methods inside ClientWithSecurity to be changed.<br>
So add your file name in place of rr.txt to be sent.<br>
<br>

#TODO:<br>
Optimise byte array sizes for file sending to reduce overly dramatic effect. From AES using byte arrays which are large and just mass encrypt/sending.




#PROGRESS TRACKER
Create file sending code -check <br>
Create file receiving code -check <br>
Create appropriate parts to make it actually run -check <br>

#Debug code - COMPLETED.  now works. 
<br>figure out the total length of an encrypted file, send it over as the expected total bytes.- Check. Formula is based on modulo you lazy.<br>
Complete verification for certificate both ways. ie. hash a preset message. you could randomise the message but that seems like extra effort with little reward. -COMPLETED<br>
Padding issue. - COMPLETED. minor error by me.<br>
Completed AES key exchange part-Check <br>
First Final part: Make sure you encode and decode AES, sending files over- in progress...-Check<br>
Second Final Part: Add a timer, take times, take out all System Print Lines other then required. - Check<br>    
Final part: Do check across 2 systems. -CHECK <br>
Fix their horrible send and receive code - check<br>
<br>note: apparently you should just send the entire expected filesize and CUT as soon as required.<br>