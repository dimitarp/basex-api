# This example shows how queries can be executed in an iterative manner.
# Documentation: http://basex.org/api
#
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

require 'BaseXClient.rb'

begin
  # create session
  session = Session.new("localhost", 1984, "admin", "admin")

  begin
    # create query instance
    input = "for $i in 1 to 10 return <xml>Text { $i }</xml>"
    query = session.query(input)

    # loop through all results
    while query.more do
      print query.next
    end

    # close query instance
    query.close()
  
  rescue Exception => e
    # print exception
    puts e
  end

  # close session
  session.close

rescue Exception => e
  # print exception
  puts e
end