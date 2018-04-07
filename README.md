# 50-005 Programming Assignment 2
Simple Server-Client codes.
Initate classes, and pass in the arguments as required. Given how insecure the code is, i'm not documenting it.<br>
<br><br>
Change of plans. Guess I am. <br>
To make it work in an IDE like intellij, please place it into the outermost folder<br>
For the rest, usual commandline compile please.<br>
<br><br><br>
#PROGRESS TRACKER
Create file sending code -check <br>
Create file receiving code -check <br>
Create appropriate parts to make it actually run -check <br>

#Debug code - in progress... will be updated with new horrible things found.
Fix their horrible send and receive code - check<br>
<br>note: apparently you should just send the entire expected filesize and CUT as soon as required.<br>
don't do their stupid flag method that just screwed it all over<br>
<br>figure out the total length of an encrypted file, send it over as the expected total bytes.- Check. Formula is based on modulo you lazy.<br>
Complete verification for certificate both ways. ie. hash a preset message. you could randomise the message but that seems like extra effort with little reward. -COMPLETED<br>
Padding issue. - COMPLETED. minor error by me.<br>
Completed AES key exchange part-Check <br>
First Final part: Make sure you encode and decode AES, sending files over- in progress...-Check<br>
Second Final Part: Add a timer, take times, take out all System Print Lines other then required.-Check<br>    
Final part: Do check across 2 systems. <br>
