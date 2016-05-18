import sys
import random
import numpy

def get_mean_from_cdf(cdf):
  prevCdf = 0;
  mean = 0.0
  for a in cdf:
    interval = a[1] - prevCdf
    prevCdf = a[1]
    mean += interval * a[0]
  return mean

# Command line arguments
if len(sys.argv) < 6:
  sys.stderr.write("Usage: %s size_cdf arity_fattree load bandwidth endtime"%(sys.argv[0]))
  sys.exit(1)

arity = int(sys.argv[2])
ncore_switches = (arity / 2) ** 2
pods = arity
switches_assigned_so_far = ncore_switches
links = []
hosts = []
for p in xrange(0, pods):
  # Agg singly connected to core
  aggs = []
  for agg in xrange(0, arity / 2):
    aggs.append(agg + switches_assigned_so_far)
    for agg_connect in xrange(0, arity / 2):
      links.append(((arity/2)*agg + agg_connect, agg + switches_assigned_so_far))
      #print "%d %d"%((arity/2)*agg + agg_connect, agg + switches_assigned_so_far)
  switches_assigned_so_far += (arity/2)
  tors = []
  for tor in xrange(0, arity/2):
    tors.append(tor + switches_assigned_so_far)
    for agg in aggs:
      links.append((agg, tor+switches_assigned_so_far))
      #print "%d %d"%((agg, tor + switches_assigned_so_far))
  switches_assigned_so_far += (arity/2)
  for tor in tors:
    for host in xrange(0, arity / 2):
      links.append((tor, host + switches_assigned_so_far))
      hosts.append((host + switches_assigned_so_far,len(links)-1))
      #print "%d %d h"%(tor, host + switches_assigned_so_far)
    switches_assigned_so_far += (arity / 2) # These are actually hosts

#print links
#print switches_assigned_so_far
#print hosts


#assigning flows
def invTransformSampling(cdf):
  u = random.random()
  for v, p in cdf:
    if p > u:
      return v
  return cdf[-1][0]

flow_size = open(sys.argv[1])

flow_size_cdf = map(lambda x: tuple(map(float, x.split(','))), flow_size.readlines())
mean_flow_size_bits = 8 * get_mean_from_cdf(flow_size_cdf) / 1460.0 * 1500.0
sys.stderr.write(str(mean_flow_size_bits)+'\n')
num_servers = len(hosts)
load_factor = float(sys.argv[3])
link_rate = float(sys.argv[4]) * 1000000000 # actual bandwidth; user says in Gbps

endtime = float(sys.argv[5]) 
poisson_mean = mean_flow_size_bits * (num_servers-1) / (link_rate * load_factor)
sys.stderr.write(str(poisson_mean) + '\n')

flows = []
for i in xrange(0, len(hosts)):
  for j in xrange(0, len(hosts)):
    if (i != j):
      time = 0
      while(time < endtime):
        interrarrtime = numpy.random.exponential(poisson_mean)
        time = time + interrarrtime
        size = invTransformSampling(flow_size_cdf)
        size = int(size)
        sender = hosts[i][0]
        dest = hosts[j][0]
        if (time <= endtime):
          flows.append([time, size, sender, dest])

print len(flows)
for f in flows:
  print f[0], f[1], f[2], f[3]

