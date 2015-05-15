import json
import jsonLine
import ibm_db



class PreparedStatement:
    
    
    def __init__(self, jLineObj, dbconnection, tblStatList):
        self.stmtId = jLineObj.statementId
        self.sqlStmt = jLineObj.sql
        self.stmtHdl = ibm_db.prepare(dbconnection, self.sqlStmt)
        self.isSelect = False
        self.resultSetOrdered = False
        if('order' in self.sqlStmt.lower()):
            self.resultSetOrdered = True
        if('select' in self.sqlStmt.lower()):
            self.isSelect = True
        self.resultSet = None
        self.hasResultSet = False
        self.tblName = self.__getTblName(tblStatList, jLineObj)
        self.timeStampFlag = self.__getTimeStampFlag(tblStatList, jLineObj)
        
        
    def __getTblName(self, tblList, jline):
        for tbl in tblList:
            if tbl.tblName in jline.sql:
                return tbl.tblName
            else:
                return None
    
    def __getTimeStampFlag(self, tblList, jline):
        for tbl in tblList:
            if tbl.tblName in jline.sql:
                return tbl.dateTimeFlag
            else:
                return None
        
        
    
        
    
        
   
                
    