import ibm_db
import bindings
import preparedStatement
import jsonLine
import datetime
from test.test_popen import python
import json

def immediateOper(connection, sql, outFile):
    outFile.write("Performing immediate operation: {} \n".format(sql))
    stmt = None
    try:
        stmt = ibm_db.exec_immediate(connection, sql)
    except:
        outFile.write(ibm_db.stmt_error())
        outFile.write(ibm_db.stmt_errormsg())
        raise
    if not stmt:
        outFile.write("Operation FAILED \n")
    return stmt

def setAutoComOff(connection, outFile):
    ibm_db.autocommit(connection, ibm_db.SQL_AUTOCOMMIT_OFF)
    # will return current state of autocommit, should be 0
    ac = ibm_db.autocommit(connection)
    if ac == 0:
        outFile.write("Autocommit set to off \n")
    if ac != 0:
        outFile.write("Cannot set autocommit to off. FAIL \n")

# verify that database is logging
def rollbackTrans(connection, outFile):
    if ibm_db.rollback(connection):
        outFile.write("Rollback successful \n")
    else:
        outFile.write("Rollback FAILED \n")

def commitTrans(connection, outFile):
    if ibm_db.commit(connection):
        outFile.write("Commit successful \n")
    else:
        outFile.write("Commit FAILED \n")

def performPrepStmtBindings(connection, outFile, jLine, preparedStmtList):
    currentBindings = jLine.bindingValueTuple
    outFile.write("Values to bind to current prepared statement: {} \n".format(currentBindings))
    currentSqlOper = preparedStmtList.getStmtIdSql(jLine.statementId)
    outFile.write("Executing sql statement: {} \n".format(currentSqlOper))
    currentHdl = preparedStmtList.getCurrentHdl(jLine.statementId)
    stmt = ibm_db.execute(currentHdl, currentBindings)
    if not stmt:
        outFile.write("operation FAILED")    

def performPreparedStmt(connection, outFile, jLine, preparedStmtList):
    currentSqlOper = getPreparedStmtSql(jLine.statementId, preparedStmtList)
    outFile.write("Executing sql statement: {} \n".format(currentSqlOper))    
    currentHdl = getPreparedStmtHdl(jLine.statementId, preparedStmtList)
    if not ibm_db.execute(currentHdl):
        outFile.write("operation FAILED")    

def prepareBindings(connection, outFile, jLine, preparedStmtList):
    outFile.write("Binding values: {} \n".format(jLine.bindings))
    currentSqlOper = getPreparedStmtSql(jLine.statementId, preparedStmtList)
    currentHdl = getPreparedStmtHdl(jLine.statementId, preparedStmtList)                 
    for paramDict in jLine.bindings:
        stmt = ibm_db.bind_param(currentHdl, paramDict['index'], paramDict['value'])
        if not stmt:
            outFile.write("operation FAILED")

def expectedEqualsActual(currentHdl, jLine, isOrdered, outFile):
    expectedResultsList = jLine.expectedResults
    fetchNum = jLine.nfetch
    actualResultList = getActualResultList(currentHdl, fetchNum)
    outFile.write("Expected Results: {} \n".format(expectedResultsList))
    outFile.write("Actual Results: {} \n".format(actualResultList))    
    if (actualResultList == [] and expectedResultsList == []):
        return True
    if isOrdered:
        return actualResultList == expectedResultsList
    else:
        found = False 
        for entry in actualResultList:
            for dictRow in expectedResultsList:
                if dictRow == entry:
                    expectedResultsList.remove(dictRow)
                    found = True
                    break  
            if not found:
                break
        if len(expectedResultsList) > 0:
            found = False
        if found:
            return True
        else:
            return False        
    
def getActualResultList(stmtHdl, fetchNum):
    resultList = []
    if fetchNum == None:
        resultDict = ibm_db.fetch_assoc(stmtHdl)
        if not resultDict:
            return resultList
        while resultDict:
            resultList.append(resultDict)
            resultDict = ibm_db.fetch_assoc(stmtHdl)
        return resultList
    else:
        for n in range(fetchNum):
            resultDict = ibm_db.fetch_assoc(stmtHdl)
            resultList.append(resultDict)
        return resultList
  
'''      
def checkFetchResults(stmt, jsonLine, outfile):
    expectedResults = jsonLine.expectedResults
    outfile.write("Expected Results: {} \n".format(expectedResults))
    for n in range(jsonLine.nfetch):
        found = False
        resultDict = ibm_db.fetch_assoc(stmt)
        outfile.write("Actual row result: {} \n".format(resultDict))
        for dictRow in jsonLine.expectedResults:
            if dictRow == resultDict:
                expectedResults.remove(dictRow)
                found = True
                break  
        if not found:
            break
    if len(expectedResults) > 0:
        found = False
    if found:
        return True
    else:
        return False        

     fetch row (as dict), iterate through expectedResults (which is a list of dicts), removing row from expectedResults list 
    after validation, should be zero rows when done fetching. If row not found in expectedResults, then validation also fails
    
def checkNonFetchResults(actualResultList, expectedResultList outfile):
    outfile.write("Expected Results: {} \n".format(expectedResultList))
    outfile.write("Actual Results: {} \n".format(actualResultList))
    resultDict = ibm_db.fetch_assoc(stmt)
    if actualResultList == [] and expectedResultList == []:
        outfile.write("Expected results is empty and nothing returned. Results equal \n")
        return True
    found = False
    while resultDict:
        outfile.write("Actual row result: {} \n".format(resultDict))
        found = False  
        for dictRow in jsonLine.expectedResults:
            if dictRow == resultDict:
                expectedResults.remove(dictRow)
                found = True
                break  
        if not found:
            break
        resultDict = ibm_db.fetch_assoc(stmt)
    if len(expectedResults) > 0:
        found = False
    if found:
        return True
    else:
        return False
        '''
   
def getPreparedStmtSql(jLineStmtID, preparedStmtList):
    for preparedStmt in preparedStmtList:
        if preparedStmt.stmtId == jLineStmtID:
            return preparedStmt.sqlStmt


def getPreparedStmtHdl(jLineStmtID, preparedStmtList):
    for preparedStmt in preparedStmtList:
        if preparedStmt.stmtId == jLineStmtID:
            return preparedStmt.stmtHdl

def getPreparedStmtObj(jLineStmtID, preparedStmtList):
    for preparedStmt in preparedStmtList: 
        if preparedStmt.stmtId == jLineStmtID:
            return preparedStmt

def removePreparedStmtFromList(jLineStmtID, preparedStmtList):
    for preparedStmt in preparedStmtList:
        if preparedStmt.stmtId == jLineStmtID:
            ibm_db.free_stmt(preparedStmt.stmtHdl)
            preparedStmtList.remove(preparedStmt)
            
def enterResultToJsonFile(fileName, result, outfile, comment = None):
    jsonDict = {}
    jsonDict['language'] = 'python'
    jsonDict['client'] = 'IBM DB2 ODBC DRIVER'
    jsonDict['server'] = 'Informix 12.10'
    jsonDict['test'] = fileName
    jsonDict['result'] = result
    jsonDict['detail'] = comment        
    json.dump(jsonDict, outfile)
    outfile.write("\n")
    
def enterExtraDataToJsonFile(outfile):
    jsonDict = {}
    jsonDict['language'] = 'python'
    jsonDict['client'] = 'IBM DB2 ODBC DRIVER'
    jsonDict['server'] = 'Informix 12.10'
    jsonDict['test'] = "dataTypeTest_INT8.json"
    jsonDict['result'] = False
    jsonDict['detail'] = "int8 not compatible"        
    json.dump(jsonDict, outfile)
    outfile.write("\n")
    jsonDict = {}
    jsonDict['language'] = 'python'
    jsonDict['client'] = 'IBM DB2 ODBC DRIVER'
    jsonDict['server'] = 'Informix 12.10'
    jsonDict['test'] = "dataTypeTest_SERIAL8.json"
    jsonDict['result'] = False
    jsonDict['detail'] = "serial8 not compatible"       
    json.dump(jsonDict, outfile)
    outfile.write("\n")
    jsonDict = {}
    jsonDict['language'] = 'python'
    jsonDict['client'] = 'IBM DB2 ODBC DRIVER'
    jsonDict['server'] = 'Informix 12.10'
    jsonDict['test'] = "dataTypeTest_NVARCHAR.json"
    jsonDict['result'] = False
    jsonDict['detail'] = "Need to investigate db_locale/client_locale"       
    json.dump(jsonDict, outfile)
    outfile.write("\n")
    