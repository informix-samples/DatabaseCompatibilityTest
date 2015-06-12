require_relative "json_line"
require 'rubygems'
require "ibm_db"


class PreparedStatement
	attr_accessor :stmtId, :sqlStmt, :stmtHdl, :isSelect, :resultSetOrdered
	attr_accessor :resultSet, :hasResultSet, :tblName, :timeStampFlag
	

	def initialize(jLineObj, connection, tblStatArray)
		@stmtId = jLineObj.statementId
		@sqlStmt = jLineObj.sql
		@stmtHdl = IBM_DB::prepare connection, jLineObj.sql
		unless @stmtHdl
			puts "failed to prepare"
			exit
		end
		@isSelect = false
		@resultSetOrdered = false
		if @sqlStmt.downcase.include?("order")
			@resultSetOrdered = true
		end
		if @sqlStmt.downcase.include?("select")
			@isSelect = true
		end
		@resultSet = nil
		@hasResultSet = false
		@tblName = getTblName(tblStatArray, jLineObj)
		@timeStampFlag = getTimeStampFlag(tblStatArray, jLineObj)
	end

	def getTblName(tblArray, jline)
		tblArray.each do |tbl|
			if jline.sql.include?(tbl.tblName)
				return tbl.tblName
			end
		end
		return nil
	end

	def getTimeStampFlag(tblArray, jline)
		tblArray.each do |tbl|
			if jline.sql.include?(tbl.tblName)
				return tbl.dateTimeFlag
			end
		end
		return nil
	end
end