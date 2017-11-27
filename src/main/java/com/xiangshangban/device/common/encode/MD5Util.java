package com.xiangshangban.device.common.encode;

/**
 * Created by liuguanglong on 2017/10/24.
 */

import java.security.MessageDigest;

public class MD5Util {
    // 公盐
    private static final String PUBLIC_SALT = "demo";
    // 十六进制下数字到字符的映射数组
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f"};

    /**
     * 用户密码加密，盐值为 ：私盐+公盐
     *
     * @param password 密码
     * @param salt     私盐
     * @return MD5加密字符串
     */
    public static String encryptPassword(String password, String salt) {
        return encodeByMD5(PUBLIC_SALT + password + salt);
    }

    /**
     * md5加密算法
     *
     * @param originString
     * @return
     */
    private static String encodeByMD5(String originString) {
        if (originString != null) {
            try {
                // 创建具有指定算法名称的信息摘要
                MessageDigest md = MessageDigest.getInstance("MD5");
                // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
                byte[] results = md.digest(originString.getBytes());
                // 将得到的字节数组变成字符串返回
                String resultString = byteArrayToHexString(results);
                return resultString.toUpperCase();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 转换字节数组为十六进制字符串
     *
     * @param
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static void main(String[] args) {

        String test = "{\"command\":{\"ACTION\":\"UPDATE_USER_LABEL\",\"ACTIONCode\":\"2003\",\"subCMDID\":\"1\",\"superCMDID\":\"1\"},\"commandIndex\":\"1\",\"commandMode\":\"C\",\"commandTotal\":\"1\",\"commandType\":\"S\",\"data\":{\"userLabel\":{\"userFace\":{\"faceData\":\"kQPmQ0f7OYgvjVzW569e8xMe1vF/v5bxIRx1aLHmhPEVE3SyZp2Z48DteiESIMg31fq7FHq50yBuSng j0miJ1CdJT0HqDusUr  UBIMvlULprwtZCm cC6WvlBFv7oSC1G8dEymvQTk D2WS/u9pImQPUDSUb3n5oG99t2dPoSIT74w0Y4 EKaVvSqJh7vqWQW NYQTvgokqL1gbp 9cY APGDFHDy8gvi8PEVYPTa9Fr4IMA 9pQaqvXmsAr6ZUDo8/M2qu1Z8Qrx4en68AHYKvsf6NL1/BeK9GvCgPSWSnjqoYVK9iSzvPX4ZEb4/8SQ9xnalO8JgBL1rNpe9ve8JvhiUoT0oyAE W0jlvTCvjD0E8qQ9Ay/2uwcdTDysPIi8tGnpvCkfwj2Prsu9EL82vYrIkr1E9 u8 3CCPVFUR7wZNKq9mjsvPfgxir0V3ty7fsQjPSGAEz3fKq09KZUKvRYGVr2JgV49JVyVPcfP1b0/x4i81eycPQxig71Pgoc9p7qGvaykvbx/0Qs8plSEPQ9ZYT3qzc69agoOOwuGyjxIpNI9rEeyvAszJzzjS4I9oCIOPcvlHL1FJVw8HznlvOK/wbwuxfm6y7WUPH9FZLtdwzq7cYRVvUrtlr0RyK69gJqtvLoZqrtGVck8OwZOvN3lsjypYwi9AeKkPK0aWjv9qGW8WiXZvFXWML2O 4o7NZ4yPaYyMb03OtA7IQp7u8eE9rz0CeC8S8FHPNnaLjwF4J 8y68yPFj1HTywPZc8QlLoO9LyFD17sgM8xGDOvH181jxnHF889CDpu8z4mTu5BOu8lHS/PNyHzrmnNZ48VhPGvNfyBb2 k7073G7CvMYHDj1vU5q8DHjePCQv zu7kHU8M/TNu7S6SrzfCw 8Y0ouvBi bTxHw M5G24TvAN817uO2Lo8uFxhvANDhDya bE7LbXCvLkq9zwdQga9HdHfuoybLrzZPaY7 hp3PJOpXrx5 Ag9h59uPIR3ST0XbYu8C/fwu2EEurz9ohq7ld0EPT53j7zx nA7VjYfvC29ijsqMkM8D3l PKS3m7ziNKy8z9ANPRWDC7zUZ1I83iEzu9Vny7zwWFW7P3JWvIhUEbwnY4089ypSPLpfKryn0Mg7iL8ZO1FiSDzDXiA8CJS0un1LiLzbbNw8DY/tPNKTjrkM7Xs8ejm7vCrO4bvVTei6PqonvCcxpjtq6Ra6jORrvAsDmbwVjCU8TDf/vKDygbtQeMY7kmSBvEaClbxJjEe7OJbXu60hsrvVSws8vtUmuxzc9buJFzi8uewvu0r Djwad1q8xrZPPP5ETzzq8XM8KMuIPKp/5bu5Ama8P7KWuuhdtLsDeV 8otYePDeasjynk307vO6dOx61dzyWRFc8b6oNvP2agjs00B48T6rTO7SqKzzVAJw8riicvJmZdLx/kOq7jcSUO 3/u7wCbPG7ZcBEvBhptDzshky7sb4rPOS5wDsEXWq7 YY2vNl9E7xGDa47cFi8OqW1GTwzNdO71iyBPGg5VTsS 9m7ZE 8Owk4Szrnfu450khNu5TP7DxZXtq780YJPM1HJLzdyfA7tcCsOkQZIjyQ7ag8PvHLu8r08DttBFo7TlNNvBQCfDyhFrw7X8ihuzDyEzzl9aQ7GOi2ucd6trvmfxU8MyqQvMTrg7v YtK7IxqOPMV5jzz3fQM86b2rvBsyaDuigPY61J7pu6W0Tbliq6S7RvuJPB2cqryfPly8OUhtPD2vp7wtVZk8 dlVvIIZIry2Y3882h96O/9fBjsVsq 7SxUJPNzQbbwnRwe8/LG2OhMsCTzpKou8te0vvFj7MLu20p48I26fvGtePjyG 0C7op3zu5mc2LtTQKi7AAeru Fot7uGDFo7U0qHO rDIzxv5la8DhWLulGT/LpumMo7/GO9O6x7QrrmxHy7wXK5u/57gDtVp9S7WIlSOyWHEbx9xNc7U06MO2FcgDzQ qe7WIUSvNXMojsWOIa6JWCZu7TSWTtVyAe7yZhIuwqwWLyVfXE8 K9uOm 7y7uJYmG7kSDsu0DLRLoMXwC7ST5WuhERCryG/gE8hiMZu/qq3ronSWA71IScOk1/hrs8/AY7 3xUu4xNhzrpXCC8qHHQuqQTHLpdy6c7OHs9u OkHTz2/SA7GBw7uQ9EB7sptKI56zomu9CfjTs6 Ke7H8MdvPGSCTsWKci7UhOGN40cmToT5v87Qn9juUHSrzvM WM50mcru TdnTtvWHE5Hrh1O6J ILveLMG7r9SfOwJKybviiMq7bW/5un/HAjxzqVK6Nh5uOyNTn7ujdTc5iHlBuyuaUjkbDVI7 vxDuvCnLrum3Wc7VP52unw4WTpjcaS43LSYu0/qnTtiC7I66t/xO76nATrzcYw6iZchuyKj8boFt5W5KHh OV29srpFJng6YXh4O/UZtTkJV4c7dfO/OaSVdDvF3Ee7iS2dOnPTmrq3KTE71dx0u8aubLuPDLK6ciuaO2zxT7hGpBW67InhuniBHztzPQ67xPuPOsXloTrq7LO6vuXtOXWwsruZ37i6627Mu2qpCzvzWx86hENcu2RaKDq7fwk7Bfd/ul4br7pTSjE5LaujurDPmrp4zIA6j n0uHprjDpT9wg7JXqBu70kwzrgQ5g6AM xuoFeNLu9T0e6u/4Cu5EHgTvJYSw7PvxIOyN36jq/z425zz44Okp4orpxvce6TecZuQoH5LkUl466te0Bu197lLreRUU6m V uKHWDztDJEU6 0nxupAO97jjBnC6k3MNufjG DkXfck6 zIFOltxkDm0h6c5YSjASeZfc6kr6j7n71p6cEd9W8xNYnnnXll1BgTqJC8PpQds46erYKcmWvon8W80MgBIZCufvQO7BgbZnRQgvjzYr8Irvwu9YMOVSOvtRO97Qh8g9p jU0rzYxVYHXrr1S92DVwaRYmxd Mweonm/vjbmeuMu1A4A6cnS8Tq6L1y5wbw3Tjg2Fn5WZVU5AZ8haVmjwHHqIWjdtZbbiiapLWC0IM3gzqTrlWVeGxtP69YkapCdD3CPWWOCOgVTe/muZhYh3RjhvsDRbrliSpzGZUqMDUu3YuOGklIX1jNg/P0qmHdJhBDw5cdbLhXYbCqr0m7jZRULZQqRVAYTtsTGihUjbVjp6wYvrpVuaceMZv7awdbaVqpXO7XAAuRSboiwG/X3274nz ZD7yIZxD2awxB6/7iyzO090WnmToJDU9NpojnWI2BN/enmePFSn BT Mwhg9ItTikRA/nTMyFRmDuA1pEdZbU2c 7vQNgFPdqN4OAB8YhJ6hT1VgYOs5XhnQFvrRHWPAOtXSrflgpCNb6G8B5pueC7FWJigVIaK tc8/MZ U EHQmSihnCmObQwyDoADBOeVTBheAVQ==\",\"faceName\":\"赵武\",\"userId\":\"9C305EC5587745FF9F0D8198512264D6\"},\"userFinger1\":\"\",\"userFinger2\":\"\",\"userId\":\"9C305EC5587745FF9F0D8198512264D6\",\"userNFC\":\"\"}},\"deviceId\":\"0d0a21d4e6fd3cb8-1-2-265f\",\"fileEdition\":\"1\",\"outOfTime\":\"\",\"sendTime\":\"2017-11-27 14:10:52\",\"serverId\":\"0\"}";
        String abc = "{\"command\":{\"ACTION\":\"UPDATE_USER_LABEL\",\"ACTIONCode\":\"2003\",\"subCMDID\":\"1\",\"superCMDID\":\"1\"},\"commandIndex\":\"1\",\"commandMode\":\"C\",\"commandTotal\":\"1\",\"commandType\":\"S\",\"data\":{\"userLabel\":{\"userFace\":{\"faceData\":\"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\",\"faceName\":\"赵武\",\"userId\":\"9C305EC5587745FF9F0D8198512264D6\"},\"userFinger1\":\"\",\"userFinger2\":\"\",\"userId\":\"9C305EC5587745FF9F0D8198512264D6\",\"userNFC\":\"\"}},\"deviceId\":\"0d0a21d4e6fd3cb8-1-2-265f\",\"fileEdition\":\"1\",\"outOfTime\":\"\",\"sendTime\":\"2017-11-27 14:10:52\",\"serverId\":\"0\"}";
        //        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(test);
//        System.out.println(JSON.toJSONString(jsonObject));
//        System.out.println(encryptPassword(JSON.toJSONString(jsonObject), "XC9EO5GKOIVRMBQ2YE8X"));
//        String test = "icUktmQRa9nYya0hdc1nMvH9eYABCyoY0MQ4zGug583dCCB7Eu7t9bbJSCZ8 j2N6J9Gt0LQCX1VCGKdsSjHUnco+WihsOTEwdmLYWS2biBJnKZzV7shkhGMe /zK6Ec5PtUcnBMIRlInfiNnqU0oeZNsSetUoHPv5FGENlJSFSu/ycuD1/nkv 8gAa1itZ2q7fOGVtIhKz2ThzsOuCxOppP87XUNxTiRxAPuimcPFmThWFY7Lm MmkdjR6SStbOOxHVUtaVlcDdozC8IVC1Of+e3JQBMqDJWKK7R5SuQVZGMRAC CX8wJZd74y95uIi/9YDykOUqC1ixh/OLKS+FZ+dJ28g9hTFY0sjKoOZGKnba imQGogqg6/917pWwf7wYHJ41Efu1tGb2uoa34rzA70UZdzNhkWTSUdRhdEM/ o7S+Mom1LBZY11sBmud05mtp845OC4YpjdSWx1hN+buWb1pEDQ1RZls4ccDl MKPyOyJ3uCu2icFe5sN7cS/160s2b0ge0oqXRmo0iwYTPnLiBnF0KfqdYb4H 5Dyk7CgkrDHApmsbEsDiSsAg8KS0S+H3fv3kdqZalQriVyQ3Fd6YO8Rfehwt 7y2Ccv8lWNxMmiF90FqyCnRtvdMbZ3NdIMgoZT29lG4gJwhS7m2pz+IbFx3q BrhmDVR8b4wVwYsf0jD0Pdc5PRC4R1mkR7A0IAUkIVRA77jhmqKOpSDvoF/9 aT1UNum7fHs55ImDK+89uIKNEZ2fxDpkk3HvaOXyPBEfCtPmsCFTQklDm2Ff 7cwWCaAZ+7tZFguYFcNMZlthb/bhTpaFdPzvNXBB4d4rpxt9nWjndHBNkARB rnb9nxQePeuvtM5OxWd3CzgXnaWAYmK4w80hL6xBdAkufjmkBKYMWrMWOGBv PxDqVUi0EtCliTRDqQ208ykfxFL7ucvrJhhZBstGvhItpHE+bDMy630KvguA Hw4KQHiOWIdLD5TMKw5fbswq9YBjCwekdoPrHByQY7O6YAqFuw3g5XB+UeTE 2+bgNy+lPYlnVrYZF8CfwcA0UTJesqiu1OA3AROoNoT8umTQloeAgxELG932 xG247aeH8zIPUjfGkLEgkPto5Cx3AbjGzKVsiBc+IAzE+Jm0pvD/v3lQMogK 3Pp5372fCVXE8w60MsqsmqVNGz3nkLMGe89Cf9BL7avOXaVr0pvMRVrciFOQ O3tG9FwwDKfI+F2Q/ztiY+r7MuMb7tVqVEFP30kZ4df18iuBB4T12SkBMRe4 lZPkE0g3Oy40SQHIPZEdrxqtF2JquJSEvrV3Prs0/4RNRrTVjedlCysuXN/Z xCYNEy0NnszqkwEvnUdrnOv2NHABPtuyCE6F3u/GQIhpCC1rkGFCJcltLzsm FcEVDdTcc6NlA/4DdasblkdvZXhfYbkwG22d2YqvB+r5K4LZa+BsxO/xusw5 Gmg9TKuqxaxGomK7LCg+TTsVJRAumavv0+4n25TIEWJu0/w+RhXRqlrmW7KU OoGLqAjHKK/7M/Z+SKJvzkZ0oEK/l29XyyMXOlV+UnRB9QyXNFiTZ+RW4Q7A JdFwukF/ltIYgrQO3zubZhrPNZFUR+bdsTySPbd7RK094uI+j18dWnrFFbi8 oLW/52nol1oLMo0HkM8McDXta5XOf8s9W2zB76vyyTx51NhgQwZ5q/cmzaJo jclGAKCpVHsViInqIX4dExSorMiGC+wloq8zxb91+5/eVFHXCm3hrOyqx3sS hkWNL9U0m86yv9b4hj+go531m8AZw5bLDwiUlMJJ/dcFALUmz20LQVu/NTe7 1fWLLyMNqeKiYHKE7oG4I2lQM6SfRqRBrigPtG1i8n+K+4JDiqTcJoafYJqN w7YUu12rjByXuqTR7xbo6WuaG8gGb+T8FPm23Zw1fLeSDTmOE1duSSOPw2R2 wSYeDVc6v+QfjVs99ljthzTVKS5Je6eQVcHCYoZmRX3xFLwBlpQfBettt/zt lFGG8iD8uOYj4rO/f6aAD+apjU0KqOmWJa0kakdU1+ltun8PboAGjdMGzbXI 6trwIOB+M4frNMVA1eOS/Qupl9USxS3aYrdZdGocVepC8euzOwvEp/2Qo+iP 7JaFTB1Ea2wjRz4Gsz2lTBIpri3sHWv9qmJlVMQy60EYGiVItFkEhRUzGmn8 HRy3ZcTVq5rZ3s+4tFPHFB489i8Csh9TKbR50CXRWy+1bTEPJ7GXqX2xv1K4 XrRm2iYxPDDnA0Q71Gdj3OBs8bfKN93vHi9ljVqJwoPpFIk/kpyMx3WJeXQJ sOGzLkr/V1tCK/G18RPtHdLV/brVt4ZmFsPPtjaBCGUingNxM+H/xNFkAWUp HWYth6qMU3IAnptXfa//H1VIwL5uVqNp6ozi4KOPwmkaETVCf7WtQPlXedfh +5BSEdKSWL++CkqsGqNW6PPmBM06wTvqGtjs1zVyrBzTyBn2lDqj4tuYcG4H KYpTnJDDa6LY48eyCb0GXktBLJmRzXpE7a9ClTweYvxaQBebcjUKd5abZPqT pf56sEny1WbfvHaVoDS7/x3dZ74HJKDKIBQL3hTib8FG/mgkaHvAH0ly3mhv GvjovxQWyAju2E1YLpnRS2oIYgdQIkf23U2RL4mwABTlfwfafqM/6g4T0JGM vpz7CdznXHJQmCwuWzi/yAjuxSHwPY66GCprK3b+sJH4Rk7MCqg9Y+sQZJ6A dOrvK+F2W3Mzv16hiq1XMOERV8mEOsZTqKOMZDum7HlqcOa5Eqe/aHiw1g82 T83QbzvmOaPLz+vdoUW/CwgdSHRKSdnQcJTZdNZavw53izHsiXHcli24Wi1I kGaUzwAi7bXijxpJVi3pICWitkly8uXRXN8JaSXjisF1jh6PPkrPZx499fjJ EhmhbpUt5my5Q9Rx1jChXElhoUdSozG+BqJMjDdthG5abTM6KTPLP4NTdgFP dekI9YfMtiCQdCNPl8xWwb648eH1ko8Y7VPGaDchtjvz0hHxp3gdU94gdrB6 q5Cb09nCE9sVQj3wFKR4mOwpZCSVwfiZmRrwRk1uC1+Yn/GJRdJ7rj2dlUeQ EEbaLQkMmtLR2yEIWVwYHSfWgsUMv6cd+vUkD0oRFTY/7DFq+N9tb3ZU1w+/ PSQCZ40YCrrcOQCGjdxgTYyxt9p3h9IOE2FNa93iKPpedg09HNz2yX6KVqqx YZRW2r6+k0Xl8ajmQ7xT9XilPjhbHiiyi3ODrfbgSaDVHU4x5BSdbpy9OFyH 5nM+hACbL54zapR7tLqZk+InIpL23mCcWi47XrB0LoqrubzNP2ngcRQ7p9G2 rNG89dwxPMuPuLVPlJwEFEOfX+AoD3YKpRw3WoUbUlrKo65MonpzsDDjH+S9 KLbyAlte1zTw/cTKGnPMzHC5PAtR+iuvmFkDmsyB3t1SkdfBct+vhMHSL2eu my8EL2ibCqlo5iH+TW0y5XvgfDXi1SD2jid8T1p4GrCoW3zEiua3XYLwilVI +twEgYodadAOcguzKt7O4Wlu56uCRjtz85SfIk+twG2sLd26y9zcEFCD8vP7 zQsdU+pyfKeHnhrmmdMQegquqQFCqIkEEdNkS+tfS91KQOsdHw6zH8qODIwW /Aoulu2znykzIuklpFU9Vc2Nk+q+L43lhveVye7uJhxAN1b9SKIM0bGXVkci 1rN8d8VawP3jRVhbRNeSphPrmU07gMEE4Onys95Z/08eR+poXSoUho5DjPs9 UkAvXe5uZT6v7U57aPNrsNX6ovHQMtWt46cHvUS1EqcpRtFbyz0J2SAndndP DlqCaKza1ygzSoB2iCTI624p8mxj4D3IiiOQLNCLoPCwculMguBtr5H0r51a VhHgKn/djVRbUfemBknifZ6rA9dpvammVW87zCwV0n10hR2dcAuaHEp+3gyv NO6Nmr+8h+Ss6NZAmlw+v932nWZNrU5mC4P70i+QGpT3n9TOjm3w+ZeulTo4 Z+hPGQNyna32ZD5VgW2IBB7QeTiqHEfIpAf93DFlthD9rEKPEr52sF02YNQg Suba7bezLkJRBGdCR+hkRPbTx6iU7QhDkdBbJaO4Co7wkW6PVrH4PtcSWOhD 5l2gchr998RCd7eo/dSBZSMRE+cthjM5WT1tYpguuy6Az5rw6BPEFHiutGhg 4tRN0mmB587b8hiEAxQRIijt7pgEOTS19cNpvO8aiGmr8AhGbLiuqMp01kDp yzi6L1z4CzFpMdrLK2wRheXzq8Txm1SzhPw+bwMP5WKdfQkl6IvCNHxHQVjd WDQNMpyDdlw1AdAvG5iwA6GQ7Y6rc6Rzr1N5kp/70lv3H6RHZJPGzy4ic1rU H74RUZ2N8PcpovnmF2CfwUvaOSeoCcotnpF5ytT+guoDg3f7BDOtdezCkKaH z0qWfDOfpr28L6yunPY1Kin+59ii5rqGQt2avTj+bDMvOGl38tK8kYrR52Pr pt8siAKlE6J5VCjvCBYW7cSTdb5rgZEfBpggIFV9dduZUacGKdwvNj8QspDJ hnZHEHIj3BPDDiwHsvENiWmqqnrDmARBGn8s8bqI9CYQ/cY12egMFwJmkY9u jRtf+FBU+hzIt1obGPPAb1s1epNmibxGAmt3Ay5vtZXWITwOPhTc5xVCQanm aIBlEJJKuDGHpV96K4PdFfF3tK7u4K6H2iGYto+yQDt2xdA+rSe0Z5LFvMwk 9UBf3gACKfbfEC+TUYmB9qpWzrc5qlBiyzw9+ZJ6U0mLJNejJ6cEj5X2Sk02 2HgvA0m0CbMjGjalBd9nTvvzttQKCFa9CgCDXMJbW5W5R8+r5n3XlwHQlafw f7cvQIj0Z5wLS02fNBhVq1tEN8HZ13Jel/Y9lxNTNQ7kvfmir9mFbmbT94F7 YZeg84b1hH0IxVGdGubVkBOVD19uNwb4Z1a+7lhvbRz3TzXF4n0D0ws3U1M1 8hTY8qVfyiOwJUl00sEpQOgUl7VfmcupjDGkEfc9tyxpV0qrnz6g/AXiZ6mg D7erkWVlPEvGekedIOD/xrCfEZayO3R0XKTpWGWDERaqlrJ1nb3iQfd3IaqG JFcFTQdu7JVGUUfP7vjYtfuMYZ5gczW3sU6jVKQu+cEZwEEp73z83QlpfX5Q i84s6GVvQwCdn5OQA/XA8B+oZnOEboVcrfwRyElGV/rtlZek+ThHxyqIaIEe 5gxyzZhC6bnzABdzSUZr3TUiHg7jhX9oMDkt60RWIsYduhCOFy0CTkFhXBFV WSs4gCE=\n";
        if (test.equals(abc)){
            System.out.println("相同");
        }

        System.out.println(encryptPassword(test, "XC9EO5GKOIVRMBQ2YE8X"));

    }
}
