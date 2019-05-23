import random

n = input("input the number of n:\n")
lb = input("Lower bound:\n")
ub = input("Upper bound\n")
length = int(ub)-int(lb)+1
lbt = input("Lower time and price bound:\n")
ubt = input("Upper time and price bound:\n")

file_name = n+"_"+str(length)+"_"+lb+"_"+ub+"_"+lbt+"_"+ubt+".txt"
print(file_name)
with open(file_name,"w") as f:
    for i in range(int(n)*int(length)):
        tmp = str(str(random.randint(int(lbt),int(ubt)+1)) +"  "+str(random.randint(int(lbt),int(ubt)+1))+"\n")
        f.write(tmp)