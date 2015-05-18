import jsonLine

class Bindings:
    def __init__(self, jsonLine):
        valueList = []
        for paramDict in jsonLine.bindings:
            # need to check type to determine
            # if any conversion needed
            if paramDict['type'] == "BOOLEAN":
                print (paramDict['value'])
                print (type(paramDict['value']))
                value = self.convertBool(paramDict['value'], jsonLine)
                print (value)
                print (type(value))
                valueList.append(value)
            else:
                valueList.append(paramDict['value'])
        valueTuple = tuple(valueList)
        self.valueTuple = valueTuple
        
    def convertBool(self, boolValue, JsonLine):
        if JsonLine.statementId == "insert":
            return boolValue
        else:
            if boolValue:
                return 1
            else:
                return 0
        