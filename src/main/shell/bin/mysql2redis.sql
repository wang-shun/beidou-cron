SELECT CONCAT(
            "*4\r\n",
            '$', LENGTH(redis_cmd), '\r\n',
            redis_cmd, '\r\n',
            '$', LENGTH(redis_key), '\r\n',
            redis_key, '\r\n',
            '$', LENGTH(hkey), '\r\n',
            hkey, '\r\n',
            '$', LENGTH(hval), '\r\n',
            hval, '\r'
)
FROM (
            SELECT
            'HSET' as redis_cmd,
            'sitekv' AS redis_key,
            sign AS hkey,
            literal AS hval
            FROM beidouurl.sitekv
) AS t

