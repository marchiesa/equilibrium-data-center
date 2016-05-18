for a in 6
do
    for tl in 1 2 3 4 5  6 7 8 9 
    do
        python create_workload.py size_cdf_ton_ecmp $a 0.$tl 10 20 > sample_workload_a"$a"_t"$tl".txt
    done
done
