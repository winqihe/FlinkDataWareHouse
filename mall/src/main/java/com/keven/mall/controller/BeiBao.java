package com.keven.mall.controller;

import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author KevenHe
 * @create 2022/4/26 16:03
 */
public class BeiBao {

    // 背包容量
    private static Integer capacity;

    // 物品个数
    private static Integer num;

    // 物品重量数组
    private static Integer[] weight;

    // 物品存放数组【0：不存放 1：存放】
    private static Integer[] store;

    // 物品最优装载数组序号【0：不存放 1：存放】
    private static Integer[] bestIndex;

    // 物品价值数组
    private static Integer[] price;

    // 背包当前重量
    private static Integer currentWeight = 0;

    // 背包当前价值
    private static Integer currentPrice = 0;

    // 背包最优价值
    private static Integer bestPrice = 0;

    /**
     * 初始化数据
     */
    private static void initData() {
        Scanner input = new Scanner(System.in);
        System.out.println("请输入背包容量:");
        capacity = input.nextInt();

        System.out.println("请输入物品数量:");
        num = input.nextInt();

        weight = new Integer[num];
        store = new Integer[num];
        bestIndex = new Integer[num];
        System.out.println("请输入各个物品的重量:");
        for (int i = 0; i < weight.length; i++) {
            weight[i] = input.nextInt();
            store[i] = 0;
            bestIndex[i] = 0;
        }

        price = new Integer[num];
        System.out.println("请输入各个物品的价值:");
        for (int i = 0; i < price.length; i++) {
            price[i] = input.nextInt();
        }
    }

    /**
     * 根据物品价值降序排列，同时调整物品重量数组
     */
    private static void sortDesc() {
        Integer temp;
        int change = 1;
        for (int i = 0; i < price.length && change == 1; i++) {
            change = 0;
            for (int j = 0; j < price.length - 1 - i; j++) {
                if (price[j] < price[j + 1]) {
                    temp = price[j]; price[j] = price[j + 1]; price[j + 1] = temp;

                    temp = weight[j]; weight[j] = weight[j + 1]; weight[j + 1] = temp;

                    change = 1;
                }
            }
        }
    }

    /**
     * 计算上届【判断】
     */
    private static Integer bound(int i) {
        Integer cleft = capacity - currentWeight;   // 记录剩余背包的容量
        Integer p = currentPrice;                   // 记录当前背包的价值

        //【已经按照物品价值降序排列，只要物品能装下，价值一定是最大】物品装入背包时，一定要确保背包能装下该物品
        while(i < weight.length && weight[i] <= cleft) {
            cleft -= weight[i];
            p += price[i];
            i++;
        }

        // 将第 i + 1 个物品切开，装满背包，计算最大价值
        if (i < weight.length) {
            p = p + cleft * (price[i] / weight[i]);
        }
        return p;
    }

    /**
     * 回溯寻找最优价值
     */
    private static void backtrack(int i) {
        // 递归结束条件
        if (i == price.length) {
            if (currentPrice > bestPrice) {
                for (int j = 0; j < store.length; j++) {
                    bestIndex[j] = store[j];
                }
                bestPrice = currentPrice;
            }
            return;
        }
        if (currentWeight + weight[i] <= capacity) {    // 确保背包当前重量 + 物品 i 的重量 <= 当前背包容量，才有意义继续进行
            store[i] = 1;
            currentWeight += weight[i];
            currentPrice += price[i];
            backtrack(i + 1);
            currentWeight -= weight[i];
            currentPrice -= price[i];
        }
        // 剪枝函数【判断 (背包当前价值 + 未确定物品的价值) 大于 背包最优价值，搜索右子树；否则剪枝】
        if (bound(i + 1) > bestPrice) {
            store[i] = 0;
            backtrack(i + 1);
        }
    }

    /**
     * 输出
     */
    private static void print() {
        System.out.println("\n降序后各个物品重量如下:");
        Stream.of(weight).forEach(element -> System.out.print(element + " "));
        System.out.println();
        System.out.println("降序后各个物品价值如下:");
        Stream.of(price).forEach(element -> System.out.print(element + " "));
        System.out.println();
        System.out.println("物品最优装载数组序号【0：不装载 1：装载】:");
        Stream.of(bestIndex).forEach(element -> System.out.print(element + " "));
        System.out.println();
        System.out.println("背包最大价值：bestPrice = " + bestPrice);
    }

    public static void main(String[] args) {
        // 初始化数据
        initData();

        // 根据物品价值降序排列，同时调整物品重量数组
        sortDesc();

        // 回溯寻找最优价值
        backtrack(0);

        // 输出
        print();
    }

}