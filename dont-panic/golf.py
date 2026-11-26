S=lambda:input().split();I=int;i=S();e={i[3]:I(i[4])}
exec('j=S();e[j[0]]=I(j[1])\n'*I(i[7]))
while 1:k=S();f=k[0];c=I(k[1]);print('BWLAOICTK'[f<'0'or c==e[f]or(c<e[f])==(k[2]>'M')::2])