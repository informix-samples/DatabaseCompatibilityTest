
require_relative "tableStats"

class JsonLine
	attr_accessor :hasId, :hasSql, :hasResults, :hasBindings, :modified
	attr_accessor :modifiedDate, :modifiedDateTime, :resource, :action
	attr_accessor :nfetch, :url, :sql, :statementId, :bindings, :bindingValueTuple
	attr_accessor :expectedResults

	def initialize(data, tblStatsList, preparedStatementList)
		@hasId = false
		@hasSql = false
		@hasResults = false
		@hasBindings = false
		@modified = false
		@modifiedDate = false
		@modifiedDateTime = false
		@resource = data["resource"]
		@action = data["action"]
		@nfetch = nil
		if data.has_key?("url")
			@url = data["url"]
		end
		if data.has_key?("sql")
			@sql = data["sql"]
			@hasSql = true
			if self.sql.include?("CREATE TABLE")
				tblName = @sql.split()[2]
				tblExists = false
				tblStatsList.each do |stat|
					if stat.tblName == tblName
						tblExists = true
					end
				end
				unless tblExists
					newTblStat = TableStats.new(@sql)
					tblStatsList.push(newTblStat)
				end
			end
			if @sql.include?("DROP TABLE")
				tblName = @sql.split()[4]
				tblStatsList.each do |stat|
					if stat.tblName == tblName
						tblStatsList.delete(stat)
					end
				end
			end
		end
		if data.has_key?("statementId")
			@statementId = data["statementId"]
			@hasId = true
		end
		if data.has_key?("bindings")
			@bindings = data["bindings"]
			#modifyBindings()
			@bindingValueTuple = getBindTuple()
			@hasBindings = true
		end
		if data.has_key?("expectedResults")
			@expectedResults = data["expectedResults"]
			if @modified == true
				modifyResults(preparedStatementList)
			end
			@hasResults = true
		end
		if data.has_key?("nfetch")
			@nfetch = data["nfetch"]
		end
	end


#	def convertToDateTime(timeStamp)
#	end

	def modifyBindings
		@bindings.each do |bindHash|
			#if bindHash.value?("DATE")
			#	@modifiedDate = true
			#	@modified = true
			#end
			if bindHash.value?("DATETIME")
				@modifiedDateTime = true
				@modified = true
			end
		end
	end

	def modifyResults(preparedStmtList)
		if @modifiedDateTime
			timeFlag = getTimeFlag(preparedStmtList)
			if timeFlag == "seconds"
				@expectedResults.each do |resultHash|
					resultHash.each do |key, value|
						resultHash[key] = resultHash[key].strftime("%Y-%m-%d %H:%M:%S")
					end
				end
			else
				@expectedResults.each do |resultHash|
					resultHash.each do |key, value|
						resultHash[key] = resultHash[key].strftime("%Y-%m-%d %H:%M:%S.%f")
					end
				end
			end
		end
	end


	def getBindTuple
		valueArray = []
		@bindings.each do |bindHash|
			valueArray.push(bindHash["value"])
		end
		return valueArray
	end

	def getTimeFlag(preparedStmtList)
		preparedStmtList.each do |stmt|
			if stmt.stmtId == @statementId
				return stmt.timeStampFlag
			end
		end
	end
end
