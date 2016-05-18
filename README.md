# Comparison among ECMP, EQUILIBRIUM, and HEDERA-SA

To reproduce the simulation results contained in our paper [1], you must first generate the topologies with the `topology/create-topology.sh` script and the workloads with the `workloads/create_workload.sh` script. Then, use the 'simulator-parameters.cfg' to configure the simulator with the desired parameters and execute the following command:

```
java -jar ton-ecmp.jar
```

The results are stored in the 'results' folder, which has to be created by the user.
