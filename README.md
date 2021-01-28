###### ****Program for clearing queues and removing unused controller services****

1. Method _queue_: The program recursively scans all process groups from the basic. The next 
it scans all connections of the process group and remove queued flow files if found it.
2. Method _cs_: The program gets list of all controller services of the basic process group
(including ancestor or descendant depends on properties include.ancestor.groups and 
include.descendant.groups). Then the program checks referencing components, and if 
they are missing, disabling and deleting them.

To run program edit _nifi.properties_ file. Specify NiFi host, basic group ID, required parameters and 
method do you want to do. 