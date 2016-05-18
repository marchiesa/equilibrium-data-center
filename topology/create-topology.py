import itertools

parity=16
coreswitches=range(0,parity*parity/4)
totalnodes = len(coreswitches) + parity*(parity+parity*parity/4)
print totalnodes
totaledges = parity*(parity*parity/4 + parity*parity/2)
print totaledges
for i in range(0,parity):
	for j in range(0,len(coreswitches)):
		index = j/(parity/2) + (len(coreswitches) + parity)*i + len(coreswitches)
		print str(j) + "\t" + str(index)
	aggregates = range((len(coreswitches) + parity)*i + len(coreswitches),(len(coreswitches) + parity)*i + len(coreswitches)+2*j/parity+1)
	edges = range((len(coreswitches) + parity)*i + len(coreswitches) + 2*j/parity+1,(len(coreswitches) + parity)*i + len(coreswitches)+4*j/parity+1)
	pairs =list(itertools.product(edges,aggregates))
	for (edge,agg) in pairs:
		print str(agg) + "\t" +str(edge) 
	for j in range((len(coreswitches) + parity)*i + len(coreswitches)+parity,(len(coreswitches) + parity)*i + len(coreswitches)+parity+len(coreswitches)):
                index = (j-((len(coreswitches) + parity)*i + len(coreswitches)+parity))/(parity/2) + (len(coreswitches) + parity)*i + len(coreswitches)+parity/2
                print str(index) + "\t" + str(j)
