class TableStats
	attr_accessor :dateTimeFlag, :sqlStr, :tblName

	def initialize(sqlStr)
		@dateTimeFlag = nil
		@sqlStmt = sqlStr
		@tblName = sqlStr.split()[2]

		if sqlStr.include?("DATETIME YEAR TO SECOND")
			@dateTimeFlag = "seconds"
		elsif sqlStr.include?("DATETIME YEAR TO FRACTION(1)")
			@dateTimeFlag = "fraction(1)"
		elsif sqlStr.include?("DATETIME YEAR TO FRACTION(2)")
			@dateTimeFlag = "fraction(2)"
		elsif sqlStr.include?("DATETIME YEAR TO FRACTION(3)")
			@dateTimeFlag = "fraction(3)"
		elsif sqlStr.include?("DATETIME YEAR TO FRACTION(4)")
			@dateTimeFlag = "fraction(4)"
		else
			@dateTimeFlag = "fraction(5)"
		end
	end
end

