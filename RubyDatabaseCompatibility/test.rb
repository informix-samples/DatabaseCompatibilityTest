require 'rubygems'
require 'ibm_db'

connStr = "DRIVER={IBM DB2 ODBC DRIVER};DATABASE=rubyDb;HOSTNAME=lxvm-l165.lenexa.ibm.com;PORT=8412;PROTOCOL=TCPIP;UID=informix;PWD=Ibm4pass;"
dbconn = IBM_DB.connect connStr, "informix", "Ibm4pass"
unless dbconn
	outFile.puts("Failed to get connection. Ending execution")
	abort "Failed to get Connection"
end
sqlStmt = "INSERT INTO bigint_test VALUES (?)"
stmtHdl = IBM_DB.prepare(dbconn, sqlStmt)
testSpecificResultsDir = Dir.open "testSpecificResults"
debugFile = File.open(File.join(testSpecificResultsDir, "debugFile.txt"), "w")
debugFile.puts("open and ready to write")
outFile = File.open(File.join(testSpecificResultsDir, "test.txt"), "w+:UTF-8")
	outFile.puts("File open. Starting Test")

IBM_DB.free_stmt(stmtHdl)