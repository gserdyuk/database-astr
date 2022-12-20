
/matches2/ 	{v  =$4; 
		# print $0;
		}
/matches\ /	{v2 =$4; 
		# print $0;
                # print(v, v2);
		 if (v != v2){ 
                      print v, v2;
                      print "not fit!";
                      }
		 else {
                      #print "+++", v, v2;
                      }
		}