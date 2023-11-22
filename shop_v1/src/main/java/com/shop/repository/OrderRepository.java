package com.shop.repository;

import com.shop.domain.Order;
import com.shop.domain.OrderSearch;
import com.shop.dto.SimpleOrderQueryDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long orderId) {
        return em.find(Order.class, orderId);
    }

    //JPQL 동적 쿼리를 문자로 생성하는 것은 번거롭고 실수로 인한 버그가 발생할 수 있다
    //실무에서는 QueryDsl 를 사용하자
    public List<Order> findOrders(OrderSearch orderSearch) {

        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    public List<Order> findOrdersFetch() {
        return  em.createQuery(
                "select " +
                        "o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d ", Order.class)
                .getResultList();
    }

    public List<SimpleOrderQueryDto> findOrdersToDto() {
        //new 명령어를 사용해서 JPQL 의 결과를 DTO 로 즉시 반환
        //SELECT 절에서 원하는 데이터를 직접 선택하기 때문에 성능이 향샹(생각보다 미비)
        return  em.createQuery(
                "select " +
                            "new com.shop.dto.SimpleOrderQueryDto(" +
                                    " o.id, " +
                                    "m.name," +
                                    "o.orderDate, " +
                                    "o.status, " +
                                    "d.address" +
                            ") " +
                        "from " +
                            "Order o " +
                        "join " +
                            "o.member m " +
                        "join " +
                            "o.delivery d", SimpleOrderQueryDto.class)
                .getResultList();
    }

}
