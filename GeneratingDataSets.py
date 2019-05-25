import random

#n = input("input the number of n:\n")
lb = 0
#ub = input("Upper bound\n")
#length = int(ub)-int(lb)+1
lbt = 5
ubt = 50

def hh(n,length,lb,ub,lbt,ubt):
    file_name = str(n)+"_"+str(length)+"_"+str(lb)+"_"+str(ub)+"_"+str(lbt)+"_"+str(ubt)+".txt"
    print(file_name)
    with open(file_name,"w") as f:
        for i in range(int(n)*int(length)):
            tmp = str(str(random.randint(int(lbt),int(ubt)+1)) +"  "+str(random.randint(int(lbt),int(ubt)+1))+"\n")
            f.write(tmp)

for n in range(5,35,5):
    for ub in range(50,250,50):
        uuub = ub-1
        length = int(uuub)-int(lb)+1
        hh(n,length,lb,uuub,lbt,ubt)
        


