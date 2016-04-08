# redis

docker run -d --restart=always --name redis -p 6379:6379 redis:3.0.7

root@host:~# dstack bash redis
root@host:/data# redis-cli -h 127.0.0.1 -p 6379
127.0.0.1:6379> set kello vorld
OK
127.0.0.1:6379> get kello
"vorld"
127.0.0.1:6379> quit
root@host:/data#
