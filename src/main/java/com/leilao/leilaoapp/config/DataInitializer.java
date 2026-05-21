package com.leilao.leilaoapp.config;

import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.entity.enums.ItemStatus;
import com.leilao.leilaoapp.entity.enums.UserRole;
import com.leilao.leilaoapp.repository.ItemRepository;
import com.leilao.leilaoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        criarAdmin();
        criarUsuarios();
        criarItens();
    }

    private void criarAdmin() {
        if (!userRepository.existsByEmail("admin@leilao.com")) {
            User admin = User.builder()
                    .name("Administrador")
                    .email("admin@leilao.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ROLE_ADMIN)
                    .codigoBidder(100000)
                    .build();
            userRepository.save(admin);
            log.info("✅ Admin criado: admin@leilao.com / senha: admin123");
        }
    }

    private void criarUsuarios() {
        List<Object[]> usuarios = List.of(
                new Object[]{"Carlos Mendes",    "carlos@teste.com",   "123456", 200001},
                new Object[]{"Ana Lima",         "ana@teste.com",      "123456", 200002},
                new Object[]{"Bruno Ferreira",   "bruno@teste.com",    "123456", 200003},
                new Object[]{"Juliana Costa",    "juliana@teste.com",  "123456", 200004},
                new Object[]{"Rafael Souza",     "rafael@teste.com",   "123456", 200005}
        );

        for (Object[] dados : usuarios) {
            String email = (String) dados[1];
            if (!userRepository.existsByEmail(email)) {
                User user = User.builder()
                        .name((String) dados[0])
                        .email(email)
                        .password(passwordEncoder.encode((String) dados[2]))
                        .role(UserRole.ROLE_USER)
                        .codigoBidder((Integer) dados[3])
                        .build();
                userRepository.save(user);
                log.info("✅ Usuário criado: {} (código: {})", email, dados[3]);
            }
        }
    }

    private void criarItens() {
        if (itemRepository.count() > 0) {
            log.info("ℹ️ Itens já existem no banco, pulando seed.");
            return;
        }

        User admin = userRepository.findByEmail("admin@leilao.com").orElseThrow();
        LocalDateTime agora = LocalDateTime.now();

        List<Item> itens = List.of(

            // ── 5 ATIVOS (encerram no futuro) ──────────────────────────────
            Item.builder()
                .title("Relógio Suíço Vintage Omega")
                .description("Relógio de pulso suíço da marca Omega, modelo Seamaster de 1968. Movimento automático original, pulseira de couro genuíno. Peça de colecionador em excelente estado de conservação.")
                .imageUrl("https://picsum.photos/seed/relogio/800/600")
                .startingPrice(new BigDecimal("1500.00"))
                .currentPrice(new BigDecimal("1500.00"))
                .startDate(agora.minusHours(2))
                .endDate(agora.plusHours(6))
                .status(ItemStatus.ATIVO)
                .createdBy(admin)
                .build(),

            Item.builder()
                .title("Quadro a Óleo — Paisagem Italiana")
                .description("Tela a óleo sobre linho, 80x60cm, retratando vila italiana ao entardecer. Assinada pelo artista plástico Marco Rossi, 1992. Emoldurada em madeira de lei com acabamento dourado.")
                .imageUrl("https://picsum.photos/seed/quadro/800/600")
                .startingPrice(new BigDecimal("3200.00"))
                .currentPrice(new BigDecimal("3200.00"))
                .startDate(agora.minusHours(5))
                .endDate(agora.plusHours(12))
                .status(ItemStatus.ATIVO)
                .createdBy(admin)
                .build(),

            Item.builder()
                .title("Câmera Leica M6 — Edição Limitada")
                .description("Câmera fotográfica analógica Leica M6, edição comemorativa 1994. Corpo em latão com revestimento preto. Acompanha lente Summicron 50mm f/2. Estado de colecionador, raramente usada.")
                .imageUrl("https://picsum.photos/seed/camera/800/600")
                .startingPrice(new BigDecimal("8900.00"))
                .currentPrice(new BigDecimal("8900.00"))
                .startDate(agora.minusHours(1))
                .endDate(agora.plusDays(1))
                .status(ItemStatus.ATIVO)
                .createdBy(admin)
                .build(),

            Item.builder()
                .title("Vaso de Porcelana Chinesa — Dinastia Qing")
                .description("Vaso decorativo em porcelana azul e branca, período Qing (século XIX). Motivos florais pintados à mão. Altura 42cm. Peça autenticada por especialista com certificado de procedência.")
                .imageUrl("https://picsum.photos/seed/vaso/800/600")
                .startingPrice(new BigDecimal("5500.00"))
                .currentPrice(new BigDecimal("5500.00"))
                .startDate(agora.minusHours(3))
                .endDate(agora.plusHours(18))
                .status(ItemStatus.ATIVO)
                .createdBy(admin)
                .build(),

            Item.builder()
                .title("Guitarra Gibson Les Paul 1959 (Réplica Histórica)")
                .description("Réplica oficial da lendária Gibson Les Paul Standard 1959, fabricada pelo Custom Shop da Gibson. Corpo em mogno, braço em mogno com escala de pau-ferro, captadores Burstbucker Pro.")
                .imageUrl("https://picsum.photos/seed/guitarra/800/600")
                .startingPrice(new BigDecimal("12000.00"))
                .currentPrice(new BigDecimal("12000.00"))
                .startDate(agora.minusHours(4))
                .endDate(agora.plusHours(8))
                .status(ItemStatus.ATIVO)
                .createdBy(admin)
                .build(),

            // ── 3 PENDENTES (ainda não iniciaram) ──────────────────────────
            Item.builder()
                .title("Coleção de Vinhos — Bordeaux Grand Cru 2015")
                .description("Lote com 12 garrafas de vinhos selecionados da região de Bordeaux, safra 2015. Inclui Château Margaux, Château Latour e Pétrus. Armazenados em adega climatizada. Nota média 97/100 (Robert Parker).")
                .imageUrl("https://picsum.photos/seed/vinho/800/600")
                .startingPrice(new BigDecimal("7800.00"))
                .currentPrice(new BigDecimal("7800.00"))
                .startDate(agora.plusHours(24))
                .endDate(agora.plusDays(3))
                .status(ItemStatus.PENDENTE)
                .createdBy(admin)
                .build(),

            Item.builder()
                .title("Escultura em Bronze — Touro de Wall Street (miniatura)")
                .description("Miniatura oficial da escultura 'Charging Bull' de Arturo Di Modica, em bronze maciço, edição numerada 47/200. Base em mármore negro. Altura 25cm. Acompanha certificado de autenticidade.")
                .imageUrl("https://picsum.photos/seed/touro/800/600")
                .startingPrice(new BigDecimal("2200.00"))
                .currentPrice(new BigDecimal("2200.00"))
                .startDate(agora.plusHours(48))
                .endDate(agora.plusDays(5))
                .status(ItemStatus.PENDENTE)
                .createdBy(admin)
                .build(),

            Item.builder()
                .title("Notebook Apple MacBook Pro M3 Max (Novo)")
                .description("MacBook Pro 16\" com chip M3 Max, 96GB RAM, SSD 4TB, tela Liquid Retina XDR. Cor Space Black. Produto novo, lacrado, com nota fiscal e garantia Apple de 1 ano.")
                .imageUrl("https://picsum.photos/seed/macbook/800/600")
                .startingPrice(new BigDecimal("18500.00"))
                .currentPrice(new BigDecimal("18500.00"))
                .startDate(agora.plusDays(2))
                .endDate(agora.plusDays(7))
                .status(ItemStatus.PENDENTE)
                .createdBy(admin)
                .build(),

            // ── 2 ENCERRADOS ────────────────────────────────────────────────
            Item.builder()
                .title("Moeda de Ouro — Brasil Imperial 1853")
                .description("Moeda de ouro 10.000 réis do Império do Brasil, ano 1853, reinado de Dom Pedro II. Classificação numismática VF-30. Peso 8,96g, ouro 22k. Acompanha laudo de autenticidade da Sociedade Numismática Brasileira.")
                .imageUrl("https://picsum.photos/seed/moeda/800/600")
                .startingPrice(new BigDecimal("4000.00"))
                .currentPrice(new BigDecimal("6750.00"))
                .startDate(agora.minusDays(5))
                .endDate(agora.minusHours(2))
                .status(ItemStatus.ENCERRADO)
                .createdBy(admin)
                .build(),

            Item.builder()
                .title("Tênis Nike Air Jordan 1 Retro High OG — Chicago")
                .description("Nike Air Jordan 1 Retro High OG \"Chicago\" 2022, tamanho 42 BR. Produto sem uso, na caixa original com todos os acessórios. Colorway icônica preto/vermelho/branco. Comprovante de compra incluso.")
                .imageUrl("https://picsum.photos/seed/tenis/800/600")
                .startingPrice(new BigDecimal("900.00"))
                .currentPrice(new BigDecimal("2100.00"))
                .startDate(agora.minusDays(3))
                .endDate(agora.minusHours(6))
                .status(ItemStatus.ENCERRADO)
                .createdBy(admin)
                .build()
        );

        itemRepository.saveAll(itens);
        log.info("✅ 10 itens de teste criados com sucesso!");
        log.info("   → 5 ATIVOS | 3 PENDENTES | 2 ENCERRADOS");
    }
}
