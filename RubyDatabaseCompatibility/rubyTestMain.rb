require 'rubygems'
require 'ibm_db'
require 'json'
require 'pathname'
require_relative "json_line"
require_relative "varMethods"
require_relative "preparedStatement"

#  Main file for testing database compatibility with Informix and ruby language
#$stdout.reopen("out.txt", "w")
$stderr.reopen("err.txt", "w")
totalResultsDir = Dir.open "results"
testSpecificResultsDir = Dir.open "testSpecificResults"
totalResultsFile = File.open(File.join(totalResultsDir, "results_ruby_drda.json"), "w:UTF-8")
testDirName = Pathname.pwd.parent + "JavaDatabaseCompatibility/resources"
testDir = Dir.open testDirName
Dir.foreach(testDirName) do |fileName|
	puts fileName.to_s
	$valueArray = []
	next if fileName == '.' or fileName == '..'
	# int8 and serial8 cause server to crash, therefore ignore these tests
	if fileName.include?("INT8") || fileName.include?("SERIAL8") || fileName.include?("ops.json")
		next
	end
	resultFileName = fileName.gsub("json", "txt")
	outFile = File.open(File.join(testSpecificResultsDir, resultFileName), "w:UTF-8")
	outFile.puts("File open. Starting Test")
	# using local connection for testing, use DbCredential class for deployment to bluemix
	connStr = "DRIVER={IBM DB2 ODBC DRIVER};DATABASE=rubyDb;HOSTNAME=hostname;PORT=8412;"\
	"PROTOCOL=TCPIP;UID=userid;PWD=password;"
	dbconn = IBM_DB.connect connStr, "userid", "password"
=begin  for use with bluemix
	credentials = DbCredentials.new
	connStr = "DATABASE=" + credentials.db + ";HOSTNAME=" + credentials.hostName + ";PORT=" + credentials.port+\
	";UID="+credentials.uid+";PWD="+credentials.pwd"
	dbconn = IBM_DB.connect connStr
=end
	unless dbconn
		outFile.puts("Failed to get connection. Ending execution")
		abort "Failed to get Connection"
	end
	lineNo = 1 #used during output to file for tracking(debug) purposes
	# keep running list of active prepared lists based on statementId type
	preparedStmtArray = []
	tableStatsArray = []
	File.open(File.join(testDir, fileName), "r:UTF-8") do |fileHandle|
		fileHandle.each_line do |line|
			
			unless line[0] == '#'
				lineArray = JSON.parse(line)
				jLine = JsonLine.new(lineArray, tableStatsArray, preparedStmtArray)
				if jLine.resource == "session"
					if jLine.action == "startTransaction"
						VarMethods.setAutoComOff(dbconn, outFile)
					elsif jLine.action == "rollbackTransaction"
						VarMethods.rollbackTrans(dbconn, outFile)
					elsif jLine.action == "commitTransaction"
						VarMethods.commitTrans(dbconn, outFile)
					else
						outFile.puts("resource = session, nothing to do")
					end
				end
				if jLine.resource == "credentials"
					outFile.puts("resource = credentials, nothing to do")
				end
				if jLine.resource == "statement"
					if jLine.action == "close"
						outFile.puts("Close immediate action, ignoring")
					elsif jLine.action == "execute"
						returnHdl = VarMethods.immediateOper(dbconn, jLine.sql, outFile)
					end
					if jLine.hasResults
						currentHdl = VarMethods.getPreparedStmtHdl(jLine.statementId, preparedStmtArray)
						if VarMethods.expectedEqualsActual(currentHdl, jLine.expectedResults, outFile)
							outFile.puts("PASS")
						else
							outFile.puts("FAILED")
						end
					end
				elsif jLine.resource == "preparedStatement"
					if jLine.action == "create"
						VarMethods.removePreparedStmtFromList(jLine.statementId, preparedStmtArray)
						preparedStmtArray.append(PreparedStatement.new(jLine, dbconn, tableStatsArray))
						outFile.puts("Creating prepared statement: #{jLine.sql}")
					elsif jLine.action == "execute"
						if jLine.hasBindings
							begin
								VarMethods.prepareBindings(dbconn, outFile, jLine, preparedStmtArray)
							rescue
								puts $!, $@
								outFile.puts("FAILED with exception")
							end
						end
						begin
							VarMethods.performPreparedStmt(dbconn, outFile, jLine, preparedStmtArray)
						rescue
							puts $!, $@
							outFile.puts("FAILED with exception")
						end
						if jLine.hasResults and VarMethods.getPreparedStmtObj(jLine.statementId, preparedStmtArray).isSelect
							currentHdl = VarMethods.getPreparedStmtHdl(jLine.statementId, preparedStmtArray)
							isOrdered = VarMethods.getPreparedStmtObj(jLine.statementId, preparedStmtArray).resultSetOrdered
							begin
								stmt = VarMethods.expectedEqualsActual(currentHdl, jLine, isOrdered, outFile)
							rescue
								puts $!, $@
								outFile.puts("FAILED with exception")
							end
							if stmt
								outFile.puts("PASS")
							else
								outFile.puts("FAILED")
							end

						end
					elsif jLine.action == "fetch"
						currentHdl = VarMethods.getPreparedStmtHdl(jLine.statementId, preparedStmtArray)
						isOrdered = VarMethods.getPreparedStmtObj(jLine.statementId, preparedStmtArray).resultSetOrdered
						begin
							stmt = VarMethods.expectedEqualsActual(currentHdl, jLine, isOrdered, outFile)
						rescue
							puts $!, $@
							outFile.puts("FAILED with exception")
						end
						if stmt
							outFile.puts("PASS")
						else
							outFile.puts("FAILED")
						end
					
					elsif jLine.action == "close"
						outFile.puts "closing prepared statement #{VarMethods.getPreparedStmtSql(jLine.statementId, preparedStmtArray)}"
						sqlStmt = VarMethods.removePreparedStmtFromList(jLine.statementId, preparedStmtArray)
						outFile.puts "Removed: #{sqlStmt}"
					end
				end
			end
			lineNo += 1
			outFile.puts("line no: #{lineNo}")
		end
		# check for FAILED in result file
		outFile.close
		result = false
		if File.read(File.join(testSpecificResultsDir, resultFileName)).include?("FAILED")
			puts "TEST FAILED"
		else
			puts "TEST PASSED"
			result = true
		end
		comment = VarMethods.getComment(fileName)
		VarMethods.enterResultToJsonFile(fileName, result, totalResultsFile, comment)
	end

end
VarMethods.enterExtraDataToJsonFile(totalResultsFile)
totalResultsFile.close