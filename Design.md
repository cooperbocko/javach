Bootstrap:
id
port
hmap
key range
pred address
succ address

main:
lookup()
insert()
delete()

Nameserver:
id 
port 
hmap
key range
bootstrap address
pred address
succ address

main:
enter()
exit()


enter():
find node with the range: nameserver->bootstrap->nameserver->.... 
both update new key ranges, pred/succ
remove and send keys

exit():
inform and update pred/succ
update key range of succ
send over keys

lookup():
ask node if in key range
if not in range send back address of its next node
do until value is returned -> can be NULL

insert():
ask node if in key range
if not in range send back address of its next node
do until value is returned and added 

delete():
ask node if in key range
if not in range send back address of its next node
do until value is returned and deleted 