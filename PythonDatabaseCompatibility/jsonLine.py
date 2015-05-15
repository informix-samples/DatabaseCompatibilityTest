'''
Created on Mar 13, 2015

@author: GregN
'''
import json
import io, sys
import ibm_db
import datetime
import tableStats


'''
    holds info about each line in json file, modifying if necessary
    Also adds and removes tables from tableStatsList if necessary.
    '''

class JsonLine:

    def __init__(self, data, tblStatsList, preparedStatementList):
        self.hasId = False
        self.hasSql = False
        self.hasResults = False
        self.hasBindings = False
        self.modified = False
        self.modifiedDate = False
        self.modifiedDateTime = False
        self.resource = data['resource']
        self.action = data['action']
        self.nfetch = None
        if "url" in data:
            self.url = data['url']
        if "sql" in data:
            self.sql = data['sql']
            self.hasSql = True
            if "CREATE TABLE" in self.sql:
                tblName = self.sql.split()[2]
                if next((stat for stat in tblStatsList if stat.tblName == tblName), None) == None:
                    tblStatsList.append(tableStats.TableStats(self.sql))
            if "DROP TABLE" in self.sql:
                tblName = self.sql.split()[4]
                for stat in tblStatsList:
                    if stat.tblName == tblName:
                        tblStatsList.remove(stat)
        if "statementId" in data:
            self.statementId = data['statementId']
            self.hasId = True
        if "bindings" in data:
            # list of dicts
            # need to modify json input for certain data types 
            self.bindings = data['bindings']
            self.__modifyBindings() 
            self.bindingValueTuple = self.__getBindTuple()
            self.hasBindings = True
        if "expectedResults" in data:
            # it is a list of dicts
            self.expectedResults = data['expectedResults']
            if self.modified == True:
                self.__modifyResults(preparedStatementList)
            self.hasResults = True
        if "nfetch" in data:
            self.nfetch = data["nfetch"]

    
            
            
    def __convertToDateTime(self, timeStamp):
        ''' Checks to what precision timestamp is and converts to python date time
                            assumes for test purposes that trailing zeros indicates not needed precision
                            Also assumes unix timestamp is 13 digits long
                            Json files use unix timestamp for fraction 0-3.  Fraction 4-5 are given as formatted
                            strings.
                            '''      		
        try:
            yrToSecStr = str(timeStamp)[0:10]
            if self.dateTimeFlag == "seconds": # no fraction 
                return datetime.datetime.fromtimestamp(int(yrToSecStr))
            elif self.dateTimeFlag == "fraction(1)":
                partialSec = str(timeStamp)[11]
                return datetime.datetime.fromtimestamp(int(yrToSecStr)) + datetime.timedelta(deciseconds = int(partialSec))
            elif self.dateTimeFlag == "fraction(2)":
                partialSec = str(timeStamp)[11:13]
                return datetime.datetime.fromtimestamp(int(yrToSecStr)) + datetime.timedelta(centiseconds = int(partialSec))
            else:
                partialSec = str(timeStamp)[-3:]
                return datetime.datetime.fromtimestamp(int(yrToSecStr)) + datetime.timedelta(milliseconds = int(partialSec))

        except:
            print("Failed to convert")    
            raise

    def __modifyBindings(self):
        for bindDict in self.bindings:
            '''
            if "DATE" in bindDict.values(): # change from unix timestamp to python date
                providedDate = bindDict["value"]
                bindDict["value"] = datetime.date.fromtimestamp(float (providedDate) / 1000) # requires in seconds
                self.modified = True
                self.modifiedDate = True
            if "BOOLEAN" in bindDict.values():# change to char type since ibm_db converts bool to long 0/1
                if bindDict["value"] == True:
                    bindDict["value"] = 't'
                else:
                    bindDict["value"] = 'f'
                # not setting self.modified to true since result does not need modified in this case
                '''
            if "DATETIME" in bindDict.values(): 
                self.modifiedDateTime = True
                self.modified = True
                '''
                if isinstance(bindDict["value"], int): # only modify if not already in dateTime format
                    bindDict["value"] = self.__convertToDateTime(bindDict["value"])
                    '''
                    

    def __modifyResults(self, preparedStmtList): 
        '''
        if self.modifiedDate: # assuming all results are of date type, if this changes, need to change
            for resultDict in self.expectedResults: 
                for key in resultDict: 
                    providedResult = resultDict[key] 
                    resultDict[key] = datetime.date.fromtimestamp(float (providedResult) / 1000) 
                    '''
        if self.modifiedDateTime: 
            timeFlag = self.__getTimeFlag(preparedStmtList)
            if timeFlag == "seconds":
                for resultDict in self.expectedResults:
                    for key in resultDict:
                        resultDict[key] = datetime.datetime.strptime(resultDict[key], "%Y-%m-%d %H:%M:%S")
            else:
                for resultDict in self.expectedResults:
                    for key in resultDict:
                        resultDict[key] = datetime.datetime.strptime(resultDict[key], "%Y-%m-%d %H:%M:%S.%f")
            '''
            else:
                for resultDict in self.expectedResults:
                    for key in resultDict: 
                        resultDict[key] = self.__convertToDateTime(resultDict[key])
                        '''

    def __getBindTuple(self):
        valueList = []
        for bindDict in self.bindings:
            valueList.append(bindDict['value'])
        return tuple(valueList)
    
    def __getTimeFlag(self, preparedStmtList):
        for stmt in preparedStmtList:
            if stmt.stmtId == self.statementId:
                return stmt.timeStampFlag
        

