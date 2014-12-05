// This product includes color specifications and designs developed by Cynthia Brewer (http://colorbrewer.org/).
RIF.resultNames = {

  rate_unadj: {
    cl: "ratel95_unadj",
    cu: "rateu95_unadj",
    description: "Directly standardised rate adjusted for age and sex only"
  },
  rate_adj: {
    cl: "ratel95",
    cu: "rateu95",
    description: "Directly standardised rate adjusted for age, sex and provided adjustment variables"
  },

  //Relative risk ratio
  exp_unadj: {
    cl: null,
    cu: null,
    description: "Expected count adjusted for age, sex and adjustment variables "
  },
  exp_adj: {
    cl: null,
    cu: null,
    description: "Expected count adjusted for age and sex only "
  },
  rr_unadj: {
    cl: "rrl95_unadj",
    cu: "rru95_unadj",
    description: "SMR estimate of the relative risk adjusted for age and sex only "
  },
  rr_adj: {
    cl: "rrl95_adj",
    cu: "rru95_adj",
    description: "SMR estimate of the relative risk adjusted for age sex and adjustment variables "
  },

  //Empirical Bayes
  smrr_unadj: {
    cl: null,
    cu: null,
    description: "Empirical Bayes estimate of the relative risk, adjusted for age and sex only "
  },
  smrr_adj: {
    cl: null,
    cu: null,
    description: "Empirical Bayes estimate of the relative risk, adjusted for age, sex and adjustment variables "
  },

  //Bayesian Smoothing
  bym_srr_unadj: {
    cl: "bym_srrl95_unadj",
    cu: "bym_srru95_unadj",
    description: "Relative risk estimate obtained with the spatially structured term of the BYM model uniquely, adjusted for sex and age only"
  },
  bym_rr_unadj: {
    cl: "bym_rrl95_unadj",
    cu: "bym_rru95_unadj",
    description: "Relative risk estimate obtained with the BYM model, adjusted for sex and age only"
  },
  het_rr_unadj: {
    cl: "het_rrl95_unadj",
    cu: "het_rru95_unadj",
    description: "Relative risk estimate obtained with the HET model, adjusted for sex and age only"
  },
  car_rr_unadj: {
    cl: "car_rrl95_unadj",
    cu: "car_rru95_unadj",
    description: "Relative risk estimate obtained with the CAR model, adjusted for sex and age only"
  },
  bym_srr_adj: {
    cl: "bym_srrl95_adj",
    cu: "bym_srru95_adj",
    description: "Relative risk estimate obtained with the spatially structured term of the BYM model uniquely, adjusted for sex, age and adjustment variables."
  },
  bym_rr_adj: {
    cl: "bym_rrl95_adj",
    cu: "bym_rru95_adj",
    description: "Relative risk estimate obtained with the BYM model, adjusted for sex, age and adjustment variables"
  },
  het_rr_adj: {
    cl: "het_rrl95_adj",
    cu: "het_rru95_adj",
    description: "Relative risk estimate obtained with the HET model, adjusted for sex, age and adjustment variables"
  },
  car_rr_adj: {
    cl: "car_rrl95_adj",
    cu: "car_rru95_adj",
    description: "Relative risk estimate obtained with the CAR model, adjusted for sex, age and adjustment variables"
  },

};