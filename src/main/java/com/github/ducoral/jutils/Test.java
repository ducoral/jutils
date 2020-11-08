package com.github.ducoral.jutils;

import static com.github.ducoral.jutils.Core.Bean;
import static com.github.ducoral.jutils.Core.create;

public class Test {

    @Bean(type = ServiceImpl.class)
    public interface Service {
        void teste();
    }

    public static class ServiceImpl implements Service {
        private Opa opa;

        ServiceImpl(Opa opa) {
            this.opa = opa;
        }

        public void teste() {
            System.out.println("chamou teste");
            opa.opa();
        }
    }

    @Bean(type = OpaImpl.class)
    public interface Opa {
        void opa() ;
    }

    public static class OpaImpl implements Opa {
        Zica zica;
        OpaImpl(Zica zica) {
            this.zica = zica;
        }

        public void opa() {
            System.out.println("opa opa");
            zica.zica();
        }
    }

    @Bean(type = ZicaImpl.class)
    public interface Zica {

        void zica();
    }


    public static class ZicaImpl implements Zica {
        public void zica() {
            System.out.println("zica não");
        }
    }

    public static void main(String args[]) throws Exception {
        Service service = (Service) create(Service.class);
        service.teste();

    }

}
