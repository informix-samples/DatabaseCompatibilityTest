import json
import os


class DbCredentials:
    
    def __init__(self):
        ''' Currently set up to use VCAP
        but may need to adapt to use json line
        '''
        dbinfo = json.loads(os.environ['VCAP_SERVICES'])['sqldb'][0]
        dbcred = dbinfo["credentials"]
        self.db = dbcred['db']
        self.hostName = dbcred['hostname']
        self.port = dbcred['port']
        self.uid = dbcred['username']
        self.pwd = dbcred['password']
        
                     