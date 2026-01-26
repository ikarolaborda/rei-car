package com.reicar.services;

import com.reicar.dtos.ServiceOrderDTO;
import com.reicar.entities.*;
import com.reicar.entities.enums.ServiceStatus;
import com.reicar.repositories.CustomerRepository;
import com.reicar.repositories.ServiceOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository repository;
    private final CustomerRepository customerRepository;

    /**
     * Utiliza o novo método do repositório com JOIN FETCH
     * para evitar LazyInitializationException no Dashboard.
     */
    public List<ServiceOrder> findAll() {
        return repository.findAllWithCustomer();
    }

    @Transactional
    public ServiceOrder saveFromDto(ServiceOrderDTO dto) {
        // 1. Instanciação
        ServiceOrder order = "MECHANIC".equalsIgnoreCase(dto.type())
                ? new MechanicServiceOrder()
                : new TireShopServiceOrder();

        // 2. Persistência do Cliente
        Customer customer = new Customer();
        customer.setName(dto.customerName());
        customer.setPhone(dto.customerPhone());
        customer.setCity(dto.customerCity());
        customer.setState(dto.customerState());
        customerRepository.save(customer);

        // 3. Dados Básicos
        order.setCustomer(customer);
        order.setOrderNumber(generateNextOrderNumber());
        order.setEntryDate(LocalDate.now());
        order.setStatus(ServiceStatus.OPEN);

        // 4. Atribuição de campos específicos (Pattern Matching)
        if (order instanceof MechanicServiceOrder mso) {
            mso.setTechnicalDiagnosis(dto.technicalDiagnosis());
            mso.setVehicleKm(dto.vehicleKm());
        } else if (order instanceof TireShopServiceOrder tso) {
            tso.setTirePosition(dto.tirePosition());
        }

        // 5. Mapeamento de Itens
        List<ServiceItem> entityItems = dto.items().stream()
                .map(itemDto -> {
                    ServiceItem item = new ServiceItem();
                    item.setQuantity(itemDto.quantity());
                    item.setDescription(itemDto.description());
                    item.setUnitPrice(itemDto.unitPrice());
                    item.setServiceOrder(order);
                    return item;
                }).toList();
        order.setItems(entityItems);

        // 6. Cálculo do Total (Mantido no Service conforme solicitado)
        calculateTotal(order);

        return repository.save(order);
    }

    private void calculateTotal(ServiceOrder order) {
        BigDecimal markup = new BigDecimal("1.30");

        BigDecimal total = order.getItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    // Aplica 30% apenas se for mecânica
                    return (order instanceof MechanicServiceOrder) ? subtotal.multiply(markup) : subtotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalValue(total.setScale(2, RoundingMode.HALF_UP));
    }

    private String generateNextOrderNumber() {
        long count = repository.count() + 1;
        return String.format("REICAR-%d-%04d", LocalDate.now().getYear(), count);
    }
}