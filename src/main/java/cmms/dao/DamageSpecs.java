/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.dao;

import cmms.models.CauseType;
import cmms.models.Damage;
import cmms.models.Damage_;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author gpatitakis
 */
public class DamageSpecs /* implements Specification<Damage> */ {

    private static final Logger logger = Logger.getLogger(DamageSpecs.class.getName());

    public static Specification<Damage> between(Date from, Date to) {
	if (from != null && to != null) {
	    return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> builder
		    .between(root.get(Damage_.created), from, to);
	}

	return null;
    }

    public static Specification<Damage> departments(Long[] departments) {
	if (departments != null && departments.length > 0) {
	    return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> builder
		    .isTrue(root.get(Damage_.department).in((Object[]) departments));
	}

	return null;
    }

    public static Specification<Damage> groupBy() {
	return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> query
		.groupBy(root.get(Damage_.machine)).getGroupRestriction();
    }

    public static Specification<Damage> types(Long[] types) {
	if (types != null && types.length > 0) {
	    return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> builder
		    .isTrue(root.get(Damage_.type).in((Object[]) types));
	}

	return null;
    }

    public static Specification<Damage> machines(Long[] machines) {
	if (machines != null && machines.length > 0) {
	    return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> builder
		    .isTrue(root.get(Damage_.machine).in((Object[]) machines));
	}

	return null;
    }

    public static Specification<Damage> causes(Long[] causes) {
	if (causes != null && causes.length > 0) {
	    return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
		return builder.isTrue(root.get(Damage_.cause).in((Object[]) causes));
	    };
	}

	return null;
    }

    public static Specification<Damage> users(String[] users) {
	if (users != null && users.length > 0) {
	    return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> builder
		    .isTrue(root.get(Damage_.user).in((Object[]) users));
	}

	return null;
    }

    public static Specification<Damage> deleted() {
	return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> builder
		.equal(root.get(Damage_.deleted), Boolean.FALSE);
    }

    public static Specification<Damage> joinCauseType(CauseType input) {
	return (Root<Damage> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
	    Join<Damage, CauseType> causeType = root.join("type");
	    return cb.equal(causeType.get("viewName"), input);
	};
    }
}
