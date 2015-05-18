import json

''' 
    Holds information regarding table and column types
    Assumes create table format is Create Table <table name>.
    If this changes, then modification required.
    '''

class TableStats:
    
    def __init__(self, sqlStr):
        self.dateTimeFlag = None
        self.sqlStmt = sqlStr
        self.tblName = sqlStr.split()[2]
        
        if 'DATETIME YEAR TO SECOND' in sqlStr:
            self.dateTimeFlag = "seconds"
        elif 'DATETIME YEAR TO FRACTION(1)' in sqlStr:
            self.dateTimeFlag = "fraction(1)"
        elif 'DATETIME YEAR TO FRACTION(2)' in sqlStr:
            self.dateTimeFlag = "fraction(2)"
        elif 'DATETIME YEAR TO FRACTION(3)' in sqlStr:
            self.dateTimeFlag = "fraction(3)"
        elif 'DATETIME YEAR TO FRACTION(4)' in sqlStr:
            self.dateTimeFlag = "fraction(4)"
        else:
            self.dateTimeFlag = "fraction(5)"        
        