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
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository repository;
    private final CustomerRepository customerRepository; // Necessário para vincular o cliente

    @Transactional
    public ServiceOrder saveFromDto(ServiceOrderDTO dto) {
        // 1. Criar a instância correta ? mechanic : tire shop
        ServiceOrder order = createEntityByType(dto.type());

        // 2. Mapear dados do cliente (ou buscar se já existir)
        Customer customer = new Customer();
        customer.setName(dto.customerName());
        customer.setPhone(dto.customerPhone());
        customer.setCity(dto.customerCity());
        customer.setState(dto.customerState());
        customerRepository.save(customer);

        // 3. Configurar a Ordem
        order.setCustomer(customer);
        order.setOrderNumber(generateNextOrderNumber());
        order.setEntryDate(LocalDate.now());
        order.setStatus(ServiceStatus.OPEN);

        // 4. Mapear campos específicos usando pattern matching (Java 17+)
        if (order instanceof MechanicServiceOrder mso) {
            mso.setTechnicalDiagnosis(dto.technicalDiagnosis());
            mso.setVehicleKm(dto.vehicleKm());
        } else if (order instanceof TireShopServiceOrder tso) {
            tso.setTirePosition(dto.tirePosition());
        }

        // 5. Mapear Itens do Record para a Entidade
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
        calculateTotal(order);

        return repository.save(order);
    }

    private ServiceOrder createEntityByType(String type) {
        return "MECHANIC".equalsIgnoreCase(type)
                ? new MechanicServiceOrder()
                : new TireShopServiceOrder();
    }

    private void calculateTotal(ServiceOrder order) {
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalValue(total);
    }

    private String generateNextOrderNumber() {
        long count = repository.count() + 1;
        return String.format("REICAR-%d-%04d", LocalDate.now().getYear(), count);
    }
}
