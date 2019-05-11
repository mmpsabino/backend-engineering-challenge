I implemented this program using Java.

Aiming to simplify the code, I created a class- called Simple_Event - which as two attributes:
"timestamp" which is a LocalDateTime object and "duration" which is a double data type.
The Simple_Event objects are used to construct the input ArrayList, by parsing the JSON file and to construct the output ArrayList.
In both cases, the objects in the ArrayList can be viewed as pair (LocalDateTime,double).

Note: to parse the JSON file I used org.json.jar file, so we shall include it in the classpath while building and running the program.

In order to build the code, you just need to open command line and execute:

javac -cp "Path_to_json_jar" Simple_Event.java

The above command will produce the compiled applciation Simple_Event.class

To run the application, one just needs to execute the following:

java -cp "Path_to_json_jar";"Path_to_dir_where_class_file_is" Simple_Event "Path_to_json_input_file" "window_size"

