hosts=range(15,15+9)+range(30,30+9)+range(45,45+9)+range(60,60+9)+range(75,75+9)+range(90,90+9)
#print len(hosts)*(len(hosts)-1)
print str(len(hosts)-1)
for x in hosts:
	for y in [15]:
		if x!=y:
			print "0 100000000 " + str(x) + " " + str(y)
