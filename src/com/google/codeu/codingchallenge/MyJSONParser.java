// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codeu.codingchallenge;

import java.io.IOException;

import java.io.IOException;
import java.util.*;

final class MyJSONParser implements JSONParser {

  @Override
  // PARSE
  //
  // Given a string that should be a valid JSON-lite object encoded as a string
  // return the parsed object. If for any reason the string is found to be
  // invalid, the method should throw an IOException.
  
  /*
    parse method: { "key" : value }
        
    we have the two cases: value = "string"
                           value = JSON object
    
    by recursion
    */
  
  public JSON parse(String in) throws IOException {
    // TODO: implement this
    MyJSON jobj = new MyJSON ();
    
    //check the validity of the String before anything
    boolean valid = isValid(in);
    
    if(!valid)
        throw new IOException ("Invalid JSON input String");
    
    //when we call help on a new JSON, jobj it will add everything to jobj
    // while preserving the nested nature of some JSON objects
    return help(in, jobj);
  }
  
  //this method helps the parse method by adding a second parameter, location;
  //   the location allows us to specify exactly which JSON object the parsed
  //   objects need to be added to
  private JSON help(String in, JSON location) throws IOException {
      
      //if ((in.charAt(0) != '{') || (in.charAt(in.length() - 1)) != '}' )
        //throw new IOException ("Missing curly brace(s)");
      
      String key;
   
      //remove the outermost curly braces ({ })
      int i = in.indexOf('{');
      int j = in.lastIndexOf('}');
      String edit = in.substring((i + 1), j);
   
      int iColon = edit.indexOf(':');
      String temp = edit.substring(iColon + 1);
     
      int iBrace = temp.indexOf('{');  
      int iQuote = temp.indexOf('\"');
      
      //base case: the JSON object is empty: { }
      if (edit.trim().isEmpty()){
          return location;    
      }
      //first case: the key-value pair has a String as a value
      else if ((iBrace > iQuote) || (iBrace < 0)){
        
          key = getKey(edit);    
          String value = getString(edit); 
          
          int iComma = edit.indexOf(',');
          
          //if there is a comma after the String then we need to the remaining
          //   objects
          if(iComma > 0){
              String rest = edit.substring(iComma + 1);
              
              //we must add the opening and closing braces for the object to be
              //   considered valid JSON, else we cannot recursively call help
              rest = "{" + rest + "}";
              
              //we recursively call help on the rest of the input that still needs
              //   to be parsed, and add it to the JSON object returned by setString
              //   so that they are added to the same object
              return help (rest, (location.setString(key, value)) );    
          }

          //if there are no commas then we are done and add the String as it is 
          //   to the input location JSON object
          return location.setString(key, value);
      }
      //second case: the key-value pair has a JSON object as a value
      else if (iBrace < iQuote){
          int k = edit.indexOf('{');
          String obj = edit.substring(k);
   
          int jBrace = obj.indexOf('}') ;
   
          char [] chars = obj.toCharArray();

          int braces = 0;
          
          //this loop searches for the ending brace of the current object
          for(int c = 0; c < chars.length; c++){
              if(chars[c] == '{')
                  braces++;
              if (chars[c] == '}'){
                  braces--;
                  jBrace = c; 
              }
              if(braces == 0)
                  break;           
          }

          //helper function getKey(), defined below to return the key 
          key = getKey(edit);
          
          if(jBrace != obj.length()){
              obj = obj.substring(0, jBrace + 1);
          }

          String rest = edit.substring(jBrace);
      
          int iComma = rest.indexOf(',');
      
          if(iComma > 0){  
              rest = rest.substring(iComma + 1);
              rest = "{" + rest + "}";

              //we recursively call help twice, once on the rest of the input 
              //   that still needs to be parsed, and add it to the JSON object 
              //   returned by setObject (which has its own values), ensuring they
              //   are added to the same; a second time on the current object,  
              //   which is an object different from the location, this object
              //   has its own values that also need to be parsed
              return help (rest, (location.setObject(key, help(obj, (new MyJSON())))));  
          }
          //if there are no commas then we only have to parse the current object
          //   and create a new MyJSON to hold its its values
          return location.setObject(key, help(obj, (new MyJSON())));
      }
      //if none of the above cases are true the String must an invalid input
      else {      
          throw new IOException ("Invalid JSON input String");  
      }
  }
      
  //we know the key is a String so getKey() returns a String
  //param: s, a String in JSON-lite format, key : value
  //returns: the key from the key : value pairing
  private String getKey (String s){
      
      int iColon = s.indexOf(':');
   
      String key = s.substring (0, iColon);

      int i = key.indexOf("\"");
      int j = key.lastIndexOf("\"");
       
      key = key.substring((i + 1), j);

      return key;   
  }
     
  //this method is to be used when the value from the key : value pairing
  //   is a String
  //param: s, a String in JSON-lite format, key : value
  //returns: the value from the key : value pairing
  private String getString (String s){
      
      int iColon = s.indexOf(':');
     
      String value = s.substring((iColon + 1));

      int i = value.indexOf("\"");
      String edit = value.substring((i + 1));
      int j = edit.indexOf("\"");
    
      j = j + i;
    
      value = value.substring((i + 1), (j + 1));
     
      return value;
  }
  
  //checks to see if the input String is of valid JSON-lite syntax
  //   returns the corresponding boolean value
  private boolean isValid(String s){

      //removes all white space from the input String
      s = s.replaceAll("\\s","");
  
      if ((s.charAt(0) != '{') || (s.charAt(s.length() - 1)) != '}' )
          return false;
    
      String edit = s.substring(1, s.length() - 1);

      char [] chars = edit.toCharArray();
      
      //flag variable from curly braces {}
      int fBrace = 0;
      
      //state of quotes, false: not currently in a quote, true: in a quote
      boolean stateQ = false; 
      //state of what to expect next, false: expect a colon, true: expect a comma
      boolean stateNext = false; 
      //state of String, false: not in a String, true: in a String
      boolean stateS = false;
      //state of Object, false: outside of an Object, true: in an Object
      boolean stateO = false;  
      //state of comma, false: we do not need a comma, true: we need a comma
      boolean stateComma = false; 
      // state of colon, false we do not need a colon, true: we need a colon
      boolean stateColon = false;
    
    //loop through the entire array and check the conditions hold
      for(int i = 0; i < chars.length; i++){ 
      
          //starting or finishing a String
          if(chars[i] == '\"'){
              stateQ = !stateQ;
              if(stateQ)
                  stateS = true;
              else {
                  stateS = false;
                  if(!stateNext)
                      stateColon = true;
                  else
                      stateComma = true;
              }
          }
          //if open brace, increment flag variable for the braces
          else if(chars[i] == '{'){
              stateO = true;
              fBrace++;
          }
          //if closed brace, decrease flag variable
          else if(chars[i] == '}'){
              fBrace--;
          }
          //if we have a colon
          else if(chars[i] == ':'){
              
              //if the character preceeding is not a quote, then the key is not
              //   a String, we return false
              if(chars[i - 1] != '\"')          
                  return false;
              //if the character after is an open brace, we expect an object and 
              //   therefore a colon before a comma
              else if(chars[i + 1] == '{')
                  stateColon = true;
              //if we don't need a colon but got one, return false (invalid syntax)
              else if(!stateColon)
                  return false;  
              //else continue
              else{                
                  stateColon = false;
                  stateNext = true;
              }

              //if we are in a String and have colon, invalid syntax, return false
              if(stateQ)
                  return false;
          }
          //if character is a comma
          else if(chars[i] == ','){
          
              //if we are given a comma when we don't need one, this is invalid 
              //   syntax, return false
              if(!stateComma)
                  return false;

              stateComma = false;
              stateNext = false;
                
              //if we are in a String and have a comma, invalid synatx
              if(stateS)            
                  return false;
          }
          //if there are more closing curly braces than open ones at any point
          //   this is invalid so we return false
          else if(fBrace < 0)
              return false;
       
          //whenever the curly brace flag variable is 0, we are no longer in an 
          //   Object so we change the stateO (Object state) variable to false
          if(fBrace == 0)
              stateO = false;       
      }
    
      //if after the runthrough of the String curly brace variable is not 0
      //   then there are not an equal number of open and closed braces, OR
      //   if we are still in an Object OR a String then something is missing
      //   the input must be invalid
      if(fBrace != 0 || stateS || stateO)
          return false;
      
      //if all the conditions are met the String is a valid JSON-lite object
      return true;
  }
  
  
} 
