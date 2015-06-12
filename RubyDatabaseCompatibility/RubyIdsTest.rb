require 'rubygems'
require 'ibm_db'
require 'json'

#Local Variables: Local to a method (begin with a lowercase letter or _ )
#Instance Variables: across methods of an object (preceded by  (@) )
#Class Variables: across different objects of a Class (preceded by the sign @@)
#Global Variables: (preceded by ($)).

#############################
#############################
class RbTestFW
  $MyGlobalVar=101
  @@MyClassVar=202
  @MyObjVar=303
  cur_test =1
  
  #Easy Instance Variables
  attr_reader :TestFileList
  attr_writer :TestFileList

  attr_reader :test_path
  attr_writer :test_path
  
  attr_reader :test_name
  attr_writer :test_name

  def initialize(name)
    @test_name = name
	@test_path = "resources/"
	
	@TestFileList = 
	[
	  #"dataTypeTest_CHAR.json",
	  "dataTypeTest_INT.json"
	  #"try.json"
	]
	
  end
  
  def ReadTestList
    puts "ReadTestList"
  end
  
  def PrintTestName
    puts "The Test Title is : #{@test_name}"
  end
  
  def ReadJsonFileAndTest
    puts "ReadJsonFileAndTest"
  end

end

#def callTest1
def callMyFunc1
    puts "callMyFunc1"
end

##########################################
##############  TestEngn   ###############
##########################################
class TestEngn

  #Easy Instance Variables
  attr_reader :ConnStr
  attr_writer :ConnStr
  attr_reader :Conn
  attr_writer :Conn
  attr_reader :stmt
  attr_writer :stmt
  attr_reader :LastSql
  attr_writer :LastSql  
  #/////////////////////////////
  attr_reader :js_in;
  attr_writer :js_in;
  attr_reader :js_line;
  attr_writer :js_line;
  attr_reader :js_physical_line;
  attr_writer :js_physical_line;  
  attr_reader :js_resource;
  attr_writer :js_resource;
  attr_reader :js_action;
  attr_writer :js_action;
  attr_reader :js_credentialId;
  attr_writer :js_credentialId;
  attr_reader :js_sessionId;
  attr_writer :js_sessionId;
  attr_reader :js_statementId;
  attr_writer :js_statementId;
  attr_reader :js_bindings;
  attr_writer :js_bindings;
  attr_reader :js_sql;
  attr_writer :js_sql;
  attr_reader :js_type;
  attr_writer :js_type;
  attr_reader :js_value;
  attr_writer :js_value;
  attr_reader :js_expectedResults;
  attr_writer :js_expectedResults;
  #/////////////////////////////

  attr_reader :ConnState
  attr_writer :ConnState
  
  attr_reader :test_name
  attr_writer :test_name
  ##########################################################
  def initialize(name)
    @test_name = name
	@Conn = nil;
	@stmt = nil;
	@ConnState = 0;
	@LastSql = nil;
	
	@js_in = nil;
	@js_line = 0;
	@js_physical_line = 0;
    @js_resource = nil;
    @js_action = nil;
    @js_credentialId = nil;
    @js_sessionId = nil;
    @js_statementId = nil;
    @js_bindings = nil;
    @js_sql = nil;
    @js_type = nil;
    @js_value = nil;
    @js_expectedResults = nil;	
	
	@ConnStr = "DRIVER={IBM DB2 ODBC DRIVER};DATABASE=dbla;\
                       HOSTNAME=localhost;PORT=9088;PROTOCOL=TCPIP;\
                       UID=informix;PWD=password;";
  end

  ##########################################################
  def PrintJsonLine()
    puts();
	puts ("Line #{@js_physical_line} of #{@test_name}");
  end

  ##########################################################
  def PrintJsonRaw(line)
    if ( js_physical_line == line )
	   puts();
	   puts ("JSON Line #{@js_physical_line}: #{@js_in}");
    end	   
  end
  
  ##########################################################
  def PrintJsonInfo(line)
    if ( js_physical_line == line )
	   puts();
	   #puts ("JSON: #{@js_in}");
       puts( "@js_resource = #{@js_resource}");
       puts("@js_action = #{@js_action}");
       puts("@js_credentialId = #{@js_credentialId}");
       puts("@js_sessionId = #{@js_sessionId}");
       puts("@js_statementId = #{@js_statementId}");
       puts("@js_bindings = #{@js_bindings}");
       puts("@js_sql = #{@js_sql}");
       puts("@js_type = #{@js_type}");
       puts("@js_value = #{@js_value}");	   
	   
	end
  end
  
  ##########################################################
  def OpenConn
    @Conn = IBM_DB.connect( @ConnStr, "", "")
	if @Conn
	  @ConnState = 1;
	  #puts "--OpenConn : Success--"
	else
	  @ConnState = -1;
	  puts "There was an error in the connection: #{IBM_DB.conn_errormsg}"
	end
  end #OpenConn
  
  ##########################################################
  def CloseConn
    if @Conn
      #puts "--CloseConn--"
	  IBM_DB.close(@Conn)
	end
  end  #CloseConn
  
  ##########################################################
  def js_resource_session()
  #{
	  if (@js_action == "create")
		self.OpenConn
	  end
	  
	  if (@js_action == "close")
		self.CloseConn
	  end	

	  if (@js_action == "execute" )
		puts "----- DDL----"
	  end
  #}
  end #js_resource_session
  
  ##########################################################
  def js_resource_statement()
  #{
	  if (@js_action == "execute" && @js_statementId == "ddl")
		#puts "----- DDL----"
		#puts "@js_sql = #{@js_sql}";
		
        if IBM_DB.exec(@Conn, @js_sql);
           #puts "--Exec Done--"
        else
           puts IBM_DB.stmt_errormsg
		   puts "@js_sql = #{@js_sql}";
        end		
	  end
  #}
  end  #js_resource_statement
  
  
  ##########################################################  
  def js_resource_preparedStatement_js_action_create()
  #{
	if (@js_statementId == "insert" ) 
		@stmt = IBM_DB.prepare(@Conn, @js_sql);
		
  	elsif ( @js_statementId == "update") 
		@stmt = IBM_DB.prepare(@Conn, @js_sql);
		
  	elsif ( @js_statementId == "delete") 
		@stmt = IBM_DB.prepare(@Conn, @js_sql);

	elsif ( @js_statementId == "query" && @js_sql != nil ) 
        @stmt = IBM_DB.prepare(@Conn, @js_sql)
	end

    if( @js_sql != nil )
		@LastSql = @js_sql;
        #puts( "--SQL=#{@js_sql}");
    end	
  #}	  
  end #js_resource_preparedStatement_js_action_create
  
  ##########################################################
  def js_resource_preparedStatement_js_action_execute_bindings()  
  #{
  #satyan
  
  values_ary = Array.new
  ########### Do Bindings ###############
  #Tag1StartStart
  @js_bindings.each do |param|
    pindex = param["index"];
    ptype = param["type"];
    pvalue = param["value"];
	
    values_ary.push( pvalue )
	#puts( " --LastSql=          #{@LastSql}");
	bind_ptype = IBM_DB::SQL_CHAR;
    if (ptype == "CHAR")
	  bind_ptype = IBM_DB::SQL_CHAR;
    elsif ( ptype == "INT")
	  bind_ptype = IBM_DB::SQL_LONG;		 
	end

	#IBM_DB.bind_param(@stmt, pindex, param["value"], IBM_DB::SQL_PARAM_INPUT, bind_ptype ) ;		   
	IBM_DB.bind_param(@stmt, pindex, "pvalue", IBM_DB::SQL_PARAM_INPUT, bind_ptype ) ;		   
    puts( "bind_param pvalue = #{pvalue}" );
  end #Tag1StartEnd
  
  #puts( "--js_resource_preparedStatement_js_action_execute_bindings--");
  #puts( "--values_ary.count = #{values_ary.count}--");
  #puts( "--js_physical_line = #{js_physical_line}--");
  #puts( "--js_in = #{js_in}--");
  ##########################
  
  self.PrintJsonLine();
  #self.PrintJsonInfo(19);
  #self.PrintJsonRaw(19);
  
    ########### Do Execute ###############

  begin
    l_count = 0;
    if ( IBM_DB.execute(@stmt, values_ary) )
	  loop_continue = true;
	  
      while  loop_continue
	  #{
	    l_count +=  1;
	    begin
	    #puts( "--- fetch_array start----")
	    row = IBM_DB.fetch_array(@stmt);
		if( row == false)
		    puts( "l_count=#{l_count} && row==false" );
		    loop_continue = false;
		else
		    puts( "row == true" );
		end

		#puts( "--- fetch_array end----")
		rescue Exception => e
		  loop_continue = false;
		  puts( "");
		  puts( "--Exception : js_physical_line = #{js_physical_line}--");
		  puts e.message
		ensure

		  puts( "l_count=#{l_count}" );
		  puts( "loop_continue= #{loop_continue}" );
		  if ( loop_continue == false )
		   break;
		  end		
		end
		
		#puts( "-------- Satyan3 ----------" );
        puts( "loop_continue= #{loop_continue}" );
		if loop_continue == false
		   break;
		end		

		########### Compare Results ###############
		puts( "-------- Compare Results  Res = #{js_expectedResults}-- " );
        if ( js_expectedResults != nil )
	    #{
		   
		   @js_expectedResults.each do |exp_row|
		   #{
		        
				# parse columns from the row
				puts( "-------- Testing only JTesting SON.parse(exp_row) --------" )
				puts( "-------- Testing only exp_row = #{exp_row} --------" )
		        exp_row_hash = JSON.parse(exp_row)  # <<<<-- Error here yet to fix this first.
				
				col=0;
		        while (col >= 0)
   
			        exp_val = exp_row_hash["i#{col}"]
					puts( "--------Testing only #{exp_val} --------" )
					if ( exp_val == nil )
					    col = -1;
					    next;  # continue in C
					end
					
					/*
					
		            act_val = row[col];
			       
		            if( exp_val == act_val )
                       puts ( "---Value OK---" )
		            else
		               #Exp = {"i0"=>6, "i1"=>7, "i2"=>8}
			           #Act = 6
		               puts ( "");
			           puts ( "---- Error ----" )
		               puts ( "Exp = #{exp_val}" )
		               puts ( "Act = #{act_val}" )
		            end 
					
					*/
					
					col += 1;
			    end # while 
		      
		   
		   #} js_expectedResults.each
		   end
	    #}
        end	
      #}		
      end # WHILE
    else
      #puts ("Execution failed: #{IBM_DB.stmt_errormsg(@stmt)}")
	  puts("Statement Execute Failed: #{IBM_DB.getErrormsg(@stmt, IBM_DB::DB_STMT)}");
    end
  ensure
    #IBM_DB.close(conn)
  end
  

  #}
  end  #js_resource_preparedStatement_js_action_execute_bindings
  
  ##########################################################
  def js_resource_preparedStatement_js_action_execute()
  #{
    if(  (js_bindings != nil) &&
	        ( @js_statementId == "query" ||
              @js_statementId == "insert" || 
	          @js_statementId == "update" || 
	          @js_statementId == "delete") )
		
	   self.js_resource_preparedStatement_js_action_execute_bindings()
	end
  #}
  end  #js_resource_preparedStatement_js_action_execute
  
  
  
  ##########################################################
  def js_resource_preparedStatement_js_action_close()
  #{
  #}
  end  #js_resource_preparedStatement_js_action_close
  
  ##########################################################
  def js_resource_preparedStatement()
  #{
    if (@js_action == "create" )
		self.js_resource_preparedStatement_js_action_create();
		
	elsif  (@js_action == "execute"  )
		self.js_resource_preparedStatement_js_action_execute();
	
	elsif  (@js_action == "close"  )
	   #self.js_resource_preparedStatement_js_action_close();	
    end
  #}
  end #js_resource_preparedStatement
  
  ##########################################################
  def ProcessJson(json)
    @js_in = json;
	@js_line += 1;
    @js_resource = nil;
    @js_action = nil;
    @js_credentialId = nil;
    @js_sessionId = nil;
    @js_statementId = nil;
    @js_bindings = nil;
    @js_sql = nil;
    @js_type = nil;
    @js_value = nil;
    @js_expectedResults = nil;	
    
	
	#puts "----ProcessJson-------"
	
	my_hash = JSON.parse(json)
	
    @js_resource = my_hash["resource"];
    @js_action = my_hash["action"];
    @js_credentialId = my_hash["credentialId"];
    @js_sessionId = my_hash["sessionId"];
    @js_statementId = my_hash["statementId"];
    @js_bindings = my_hash["bindings"];
    @js_sql = my_hash["sql"];
    @js_type = my_hash["type"];
    @js_value = my_hash["value"];
	@js_expectedResults = my_hash["expectedResults"];
	

	
	#@js_resource={credentials, session, statement, preparedStatement}
	######## js_resource = session (2/4) ###########
	if (@js_resource == "session" )
	#{
	   self.js_resource_session();
	#} # session (2/4)
	######## js_resource = statement (3/4)###########
	elsif (@js_resource == "statement" )
	#{
	   self.js_resource_statement();
	#} # statement (3/4)
	######## js_resource = preparedStatement (4/4) ###########
	elsif (@js_resource == "preparedStatement" )
	#{
	  self.js_resource_preparedStatement();
	#} # preparedStatement (4/4)
	end		
	
  end    
  
end # TestEngn class


##########################################################
def callMyMainFunc()
  obj = RbTestFW.new( "UNKNOWN" );
  obj.PrintTestName;
  obj.test_name = "CHAR";
  obj.PrintTestName;
  #callMyFunc1

  # Run Each test
  obj.TestFileList.each do |the_test|
  #{
	obj.test_name = the_test;
    full_name = obj.test_path + the_test;
    puts "full_name is : #{full_name}" ;
	
	# Read JSON file for test instructions
    f = File.open(full_name, "r");
	
	tst = TestEngn.new( the_test );
	
    f.each_line do |json|
	#{
       #puts json
	   tst.js_physical_line += 1;
	   unless  json.start_with? '#'
	   #puts( json )
	   tst.ProcessJson(json)
	   end
	#}
    end
    f.close
	
  #}
  end
  
  obj.PrintTestName()
  
end

#############################
callMyMainFunc()
#############################
