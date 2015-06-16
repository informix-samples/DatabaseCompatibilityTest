require 'json'

class DbCredentials
	attr_accessor :db, :hostname, :port, :uid, :password

	def initialize
		vcap_services = JSON.parse(ENV('VCAP_SERVICES'))
		dbcred = vcap_services["credentials"]
		@db = dbcred["db"]
		@hostname = dbcred["hostname"]
		@port = dbcred["port"]
		@uid = dbcred["username"]
		@pwd = dbcred["password"]











