module VarMethods

	module_function

	def immediateOper(connection, sql, outFile)
		outFile.puts "Performing immediate operation: #{sql}" 
		stmt = nil
		begin
			stmt = IBM_DB.exec(connection, sql)
		rescue
			outFile.puts "#{IBM_DB.stmt_error}"
			outFile.puts "#{IBM_DB.stmt_errormsg}"

		end
		unless stmt
			outFile.puts "Operation Failed"
		end
		return stmt
	end

	def setAutoComOff(connection, outFile)
		IBM_DB.autocommit(connection, IBM_DB::SQL_AUTOCOMMIT_OFF)
		
		# verify that in correct state
		ac = IBM_DB.autocommit(connection)
		if ac == 0
			outFile.puts "Autocommit set to off as intended"
		else
			outFile.puts "Cannot set autocommit to off.  FAIL"
		end
	end

	def rollbackTrans(connection, outFile)
		if IBM_DB.rollback(connection)
			outFile.puts "Rollback successful"
		else
			outFile.puts "Rollback FAILED"
		end
	end

	def commitTrans(connection, outFile)
		if IBM_DB.commit(connection)
			outFile.puts "Commit successful"
		else
			outFile.puts "Commit FAILED"
		end
	end

	def performPrepStmtBindings(connection, outFile, jLine, preparedStmtArray)
		currentBindings = jLine.bindingValueTuple
		outFile.puts "Values to bind to current prepared statement: #{currentBindings}"
		currentSqlOper = preparedStmtArray.getStmtIdSql(jLine.statementId)
		outFile.puts "Executing sql statement: #{currentSqlOper}"
		currentHdl = preparedStmtArray.getCurrentHdl(jLine.statementId)
		begin
			stmt = IBM_DB.execute(currentHdl, currentBindings)
		rescue
			outFile.puts "#{IBM_DB.stmt_error}"
			outFile.puts "#{IBM_DB.stmt_errormsg}"
		end
		unless stmt
			outFile.puts "Operation FAILED"
		end
	end

	def performPreparedStmt(connection, outFile, jLine, preparedStmtArray)
		currentSqlOper = getPreparedStmtSql(jLine.statementId, preparedStmtArray)
		outFile.puts "Executing sql statement: #{currentSqlOper}"
		currentHdl = getPreparedStmtHdl(jLine.statementId, preparedStmtArray)
		stmt = IBM_DB::execute currentHdl
		unless stmt
			outFile.puts "Operation FAILED"
		end
	end

	#  	ibm_db.bind_param documentation states this for value parameter
	# 	'A string specifying the name of the Ruby variable to bind to the parameter specified by parameter-number.''
	# 	The bind param operation is not performed until execute is performed.  If there is more than one parameter
	# 	they need different variable names and they must by in scope when execute ran.  For this automated testing
	# 	allowances need to be made to accommodate these requirements which would not be issue in normal use

	def prepareBindings(connection, outFile, jLine, preparedStmtArray)
		stmt = nil
		currentSqlOper = getPreparedStmtSql(jLine.statementId, preparedStmtArray)
		currentHdl = getPreparedStmtHdl(jLine.statementId, preparedStmtArray)
		index = 1
		jLine.bindings.each do |paramHash|
			$valueArray[index] = paramHash["value"]
			stmt = IBM_DB::bind_param currentHdl, paramHash["index"], "$valueArray[#{index}]", IBM_DB::SQL_PARAM_INPUT
			index += 1
		end
		unless stmt
			outFile.puts "Operation FAILED"
		end
	end

	def expectedEqualsActual(currentHdl, jLine, isOrdered, outFile)
		expectedResultsArray = jLine.expectedResults
		fetchNum = jLine.nfetch
		actualResultArray = getActualResultArray(currentHdl, fetchNum, outFile)
		outFile.puts "Expected Results: #{expectedResultsArray}"
		outFile.puts "Actual Results: #{actualResultArray}"
		if (actualResultArray == [] and expectedResultsArray == [])
        	return true
    	elsif isOrdered
        	return actualResultArray == expectedResultsArray
        else
        	found = false
        	actualResultArray.each do |entry|
        		expectedResultsArray.each do |hashRow|
        			if hashRow == entry
        				expectedResultsArray.delete(hashRow)
        				found = true
        				break
        			end
        		end
        		unless found
        			break
        		end
        	end
        	if expectedResultsArray.length > 0
        		found = false
        	end
        	if found
        		return true
        	else 
        		return false
        	end
        end
    end

    def getActualResultArray(stmtHdl, fetchNum, outFile)
    	resultArray = []
    	if fetchNum == nil
			resultHash = IBM_DB.fetch_assoc(stmtHdl)
			unless resultHash 
				return resultArray
			end
			while resultHash
				resultArray.push(resultHash)
				resultHash = IBM_DB.fetch_assoc(stmtHdl)
			end
			return resultArray
		else
			fetchNum.times do |i|
				resultHash = IBM_DB.fetch_assoc(stmtHdl)
				resultArray.push(resultHash)
			end
			return resultArray
		end
	end

	def getPreparedStmtSql(jLineStmtId, preparedStmtArray)
		preparedStmtArray.each do |preparedStmt|
			if preparedStmt.stmtId == jLineStmtId
				return preparedStmt.sqlStmt
			end
		end
	end

	def getPreparedStmtHdl(jLineStmtId, preparedStmtArray)
		preparedStmtArray.each do |preparedStmt|
			if preparedStmt.stmtId == jLineStmtId
				return preparedStmt.stmtHdl
			end
		end
	end

	def getPreparedStmtObj(jLineStmtId, preparedStmtArray)
		preparedStmtArray.each do |preparedStmt|
			if preparedStmt.stmtId == jLineStmtId
				return preparedStmt
			end
		end
	end

	def enterResultToJsonFile(fileName, result, outfile, comment)
		jsonHash = {}
		jsonHash[:language] = "ruby"
		jsonHash[:client] = "IBM DB2 ODBC DRIVER"
		jsonHash[:server] = "Informix 12.10"
		jsonHash[:test] = fileName
		jsonHash[:result] = result
		jsonHash[:detail] = comment
		outfile.puts(jsonHash.to_json)
	end

	def removePreparedStmtFromList(jLineStmtId, preparedStmtArray)
		preparedStmtArray.each do |preparedStmt|
			if preparedStmt.stmtId == jLineStmtId
				IBM_DB.free_stmt(preparedStmt.stmtHdl)
				preparedStmtArray.delete(preparedStmt)
				return preparedStmt.sqlStmt
			end
		end
	end

	def enterExtraDataToJsonFile(outfile)
		jsonHash = {}
	    jsonHash[:language] = "Ruby"
	    jsonHash[:client] = "IBM DB2 ODBC DRIVER"
	    jsonHash[:server] = "Informix 12.10"
	    jsonHash[:test] = "dataTypeTest_INT8.json"
	    jsonHash[:result] = false
	    jsonHash[:detail] = "int8 not compatible"  
	    outfile.puts(jsonHash.to_json)      
	    jsonHash = {}
	    jsonHash[:language] = "Ruby"
	    jsonHash[:client] = "IBM DB2 ODBC DRIVER"
	    jsonHash[:server] = "Informix 12.10"
	    jsonHash[:test] = "dataTypeTest_SERIAL8.json"
	    jsonHash[:result] = false
	    jsonHash[:detail] = "serial8 not compatible"       
	    outfile.puts(jsonHash.to_json)
	    jsonHash = {}
	    jsonHash[:language] = "Ruby"
	    jsonHash[:client] = "IBM DB2 ODBC DRIVER"
	    jsonHash[:server] = "Informix 12.10"
	    jsonHash[:test] = "ops.json"
	    jsonHash[:result] = true
	    jsonHash[:detail] = "format of test is different than others, but operations performed in other tests"       
	    outfile.puts(jsonHash.to_json)
	end

	def getComment(fileName)
		comment = nil
		if fileName.include?("BIGINT") || fileName.include?("BIGSERIAL") 
			comment = "Driver successfully places data in database, but extracts as string"
		elsif fileName.include?("BOOLEAN")
			comment = "Driver uses 0/1 for true/false, causing certain operations to respond incorrectly"
		elsif fileName.include?("DATETIME")
			comment = "Driver successfully places data in database, but extracts with fraction format"
		elsif fileName.include?("SMALLFLOAT")
			comment = "Data fetched from database converted improperly"
		else
			comment = nil
		end
		return comment
	end


end
