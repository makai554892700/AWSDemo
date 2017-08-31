package com.mayousheng.www.utils;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.mayousheng.www.DynamoDB.Test;

import java.util.*;

public class MapperUtils {

    private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private static DynamoDBMapper mapper = new DynamoDBMapper(client);
    private static DynamoDB dynamoDB = new DynamoDB(client);
    private static String TABLE_TEST_NAME = "test";
    private static String INDEX_SEX_NAME = "sex";
    private static Table TABLE_TEST = dynamoDB.getTable(TABLE_TEST_NAME);
    private static Index INDEX_SEX = TABLE_TEST.getIndex(INDEX_SEX_NAME);
    private static final int MAX_RESULT = 10;

    public static void saveTest(Test test) {//保存用户信息
        mapper.save(test);
    }

    public static Test getTestByName(String name) {//根据用户名获取用户数据
        return mapper.load(Test.class, name,
                DynamoDBMapperConfig.ConsistentReads.CONSISTENT.config());
    }

    public static List<Test> getOWDBySex(String sex) {//根据性别获取所有数据
        List<Test> result = new ArrayList<>();
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("sex = :sex")
                .withValueMap(new ValueMap()
                        .withString(":sex", sex)
                )
                .withMaxResultSize(MAX_RESULT);
        ItemCollection<QueryOutcome> items = INDEX_SEX.query(spec);
        if (items != null) {
            Iterator<Item> iter = items.iterator();
            Test test;
            while (iter.hasNext()) {
                test = JSONObject.parseObject(iter.next().toJSON(), Test.class);
                result.add(test);
            }
        }
        return result;
    }

    public static void delTestByTest(Test test) {//删除时会自动根据唯一键值删除
        if (test != null) {
            mapper.delete(test);
        }
    }
}
