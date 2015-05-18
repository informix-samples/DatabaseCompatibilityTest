'''
    Test for compatibility of informix sqldb with python
    runtime.  
    A json file is used to provide test cases (inserts, selects, etc)
    Test parses json file and performs operation based on json entry
    
    See README.txt for details concerning setup necessary for bluemix
    environment
    
'''
   


import json  
import ibm_db  
import dbCredentials
import jsonLine
import preparedStatement
import sys 
import varMethods
import traceback
import tableStats
import os


 

if __name__ == "__main__": 
    totalResultsFile = open("totalResults.json", 'w')
    for file in os.listdir("dataTypeTestFiles"):
        print (file)
        try:
            # open result file to print remarks/results
            #os.chdir('resultFiles')
            resultFile = file.replace("json", "txt")
            outFile = open(resultFile, "w")
            outFile.write("File open. Starting tests. \n")  
            '''
            # get credential info from VCAP_SERVICES and connect to db
            credentials = DbCredentials()
            dbconn = ibm_db.connect("DATABASE=" + credentials.db + ";HOSTNAME="+credential.hostName+";PORT="+credentials.port+";UID="+credentials.uid+";PWD="+credentials.pwd+";","","")
        '''
            # using local data
            #connectString = "DRIVER={IBM INFORMIX ODBC DRIVER (64-bit)};SERVER=atmol101;DATABASE=pythondb;HOSTNAME=lxvm-l165.lenexa.ibm.com;SERVICES=atmol101;PROTOCOL=onsoctcp;UID=informix;PWD=Ifmx4spd"
            connectString = "DRIVER=(IBM DB2 ODBC DRIVER};DATABASE=pythondb;HOSTNAME=lxvm-l165.lenexa.ibm.com;PORT=8412;PROTOCOL=TCPIP;UID=informix;PWD=Ifmx4spd"
            dbconn = ibm_db.connect(connectString, None, None)
            
            if not dbconn:
                outFile.write("Failed to get connection.  Ending execution")
                sys.exit("Failed to get connection")
            outFile.write("Connection successful using connection credentials : \n {0} \n\n".format(connectString))
            lineNo = 1 # used during output to file for tracking purposes
            
            # keep running list of active prepared lists based on statementId type 
            preparedStmtList = []
            tableStatsList = []
            # iterate through json file and perform appropriate operations
            filePath = os.path.join("dataTypeTestFiles", file)
            with open(filePath, 'r') as f:
                for line in f:
                    if(line[0] != '#'):
                        # convert jsonLine to dict data
                        lineDict = json.loads(line) 
                        jLine = jsonLine.JsonLine(lineDict, tableStatsList, preparedStmtList)
                        if jLine.resource == "session":
                            if jLine.action == "startTransaction":
                                varMethods.setAutoComOff(dbconn, outFile) 
                            elif jLine.action == "rollbackTransaction":
                                varMethods.rollbackTrans(dbconn, outFile)
                            elif jLine.action == "commitTransaction":
                                varMethods.commitTrans(dbconn, outFile)                            
                            else:
                                outFile.write("resource = session, nothing to do \n")
    
                        if jLine.resource == "credentials":
                            outFile.write("resource = credentials, nothing to do \n")
                        if jLine.resource == "statement":
                            if jLine.action == "close":
                                outFile.write("Close immediate action, ignoring \n")
                            if jLine.action == "execute":
                                returnHdl = varMethods.immediateOper(dbconn, jLine.sql, outFile)
                            if jLine.hasResults:
                                # perform verification
                                currentHdl = varMethods.getPreparedStmtHdl(jLine.statementId, preparedStmtList)
                                if varMethods.expectedEqualsActual(currentHdl, jLine.expectedResults, outFile):
                                    outFile.write("PASS \n")
                                else:
                                    outFile.write("FAILED \n")
     
                        if jLine.resource == "preparedStatement":
                            
                            if jLine.action == "create":
                                # append new prepared statement.  Assume if using same Id as already there, need to remove first
                                varMethods.removePreparedStmtFromList(jLine.statementId, preparedStmtList)
                                preparedStmtList.append(preparedStatement.PreparedStatement(jLine, dbconn, tableStatsList))
                                outFile.write("Creating prepared statement: {} \n".format(jLine.sql))
                            if jLine.action == "execute":
                                if jLine.hasBindings:
                                    varMethods.prepareBindings(dbconn, outFile, jLine, preparedStmtList)
                                varMethods.performPreparedStmt(dbconn, outFile, jLine, preparedStmtList)
                                if (jLine.hasResults and varMethods.getPreparedStmtObj(jLine.statementId, preparedStmtList).isSelect):
                                    currentHdl = varMethods.getPreparedStmtHdl(jLine.statementId, preparedStmtList)
                                    isOrdered = varMethods.getPreparedStmtObj(jLine.statementId, preparedStmtList).resultSetOrdered
                                    if varMethods.expectedEqualsActual(currentHdl, jLine, isOrdered, outFile):
                                        outFile.write("PASS \n")
                                    else:
                                        outFile.write("FAILED \n")                                    
                            if jLine.action == "fetch":
                                currentHdl = varMethods.getPreparedStmtHdl(jLine.statementId, preparedStmtList)
                                isOrdered = varMethods.getPreparedStmtObj(jLine.statementId, preparedStmtList).resultSetOrdered  
                                if varMethods.expectedEqualsActual(currentHdl, jLine, isOrdered, outFile):
                                    outFile.write("PASS \n")
                                else:
                                    outFile.write("FAILED \n")                            
                            if jLine.action == "close":
                                varMethods.removePreparedStmtFromList(jLine.statementId, preparedStmtList)
                                outFile.write("closing prepared statement {}\n".format(varMethods.getPreparedStmtSql(jLine.statementId, preparedStmtList)))
                        
                    lineNo+=1
                    outFile.write("LINE NO= {} \n".format(lineNo))
                f.close()
            outFile.write("TEST DONE")
        except Exception as err:
            e = sys.exc_info()
            print(e)
            traceback.print_tb(err.__traceback__)
            outFile.write("FAILED with exception \n")
            raise
        finally:
            result = False
            outFile.close()
            ibm_db.close(dbconn)
            if "FAILED" in open(resultFile).read():
                print("Test FAILED")
                result = False
            else:
                # need to rework
                print("Test PASSED")
                result = True
            comment = None
            varMethods.enterResultToJsonFile(file, result, totalResultsFile, comment)
            f.close()
    varMethods.enterExtraDataToJsonFile(totalResultsFile)
    totalResultsFile.close()
                
            
    