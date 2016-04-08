# redis

docker run -d --restart=always --name redis -p 6379:6379 redis:3.0.7

root@was-ubuntu:~# dstack bash redis
root@6f5f9c4c0033:/data# redis-cli -h 127.0.0.1 -p 6379
127.0.0.1:6379> set kello vorld
OK
127.0.0.1:6379> get kello
"vorld"
127.0.0.1:6379> quit
root@6f5f9c4c0033:/data#
