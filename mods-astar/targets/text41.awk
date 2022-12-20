BEGIN   { sstate = 0;
          val="";
          res="";
          elim="";
          }

/\*\*\*\ e/   { 
                print( NR, val, res, elim)
                sstate=1;
                val=$6 "  " $7
           }
        
/\*\*\*\ matches2/ {
                res = $4;
                elim="";
            }

/\*\*\*\ -\ eliminated/   { # *** - eliminated
                elim=$6;
        }


























