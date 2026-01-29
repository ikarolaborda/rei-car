package com.reicar.services;

import com.reicar.dtos.ServiceOrderDTO;
import com.reicar.entities.*;
import com.reicar.entities.enums.ServiceStatus;
import com.reicar.repositories.CustomerRepository;
import com.reicar.repositories.ServiceOrderRepository;
import com.reicar.repositories.SystemConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository repository;
    private final CustomerRepository customerRepository;
    private final SystemConfigRepository systemConfigRepository;

    public List<ServiceOrder> findAll() {
        return repository.findAllWithCustomer();
    }

    public ServiceOrder findById(Long id){
        return repository.findByIdWithDetails(id).orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encotrada: id = "+id));
    }

    @Transactional
    public ServiceOrder saveFromDto(ServiceOrderDTO dto) {
        ServiceOrder order = "MECHANIC".equalsIgnoreCase(dto.type())
                ? new MechanicServiceOrder()
                : new TireShopServiceOrder();

        // Persistência do Cliente
        Customer customer = new Customer();
        customer.setName(dto.customerName());
        customer.setPhone(dto.customerPhone());
        customer.setCity(dto.customerCity());
        customer.setState(dto.customerState());
        customerRepository.save(customer);

        order.setCustomer(customer);
        order.setOrderNumber(generateNextOrderNumber());
        order.setEntryDate(LocalDate.now());
        order.setStatus(ServiceStatus.OPEN);

        order.setServiceValue(dto.serviceValue() != null ? dto.serviceValue() : BigDecimal.ZERO);

        // mAtribuição de campos específicos
        if (order instanceof MechanicServiceOrder mso) {
            mso.setTechnicalDiagnosis(dto.technicalDiagnosis());
            mso.setVehicleKm(dto.vehicleKm());
        } else if (order instanceof TireShopServiceOrder tso) {
            tso.setTirePosition(dto.tirePosition());
        }

        // Mapeamento de Itens (Suporta lista vazia para apenas mão de obra)
        if (dto.items() != null && !dto.items().isEmpty()) {
            List<ServiceItem> entityItems = dto.items().stream()
                    .map(itemDto -> {
                        ServiceItem item = new ServiceItem();
                        item.setQuantity(itemDto.quantity());
                        item.setDescription(itemDto.description());
                        item.setUnitPrice(itemDto.unitPrice());
                        item.setServiceOrder(order);
                        return item;
                    }).toList();
            order.getItems().addAll(entityItems);
        }

        // Define o markup: 1.30 (Mecânica) ou 1.0 (Borracharia)
        double markup = (order instanceof MechanicServiceOrder) ? 1.30 : 1.0;
        order.calculateTotalValue(markup);

        return repository.save(order);
    }

    private String generateNextOrderNumber() {
        long count = repository.count() + 1;
        return String.format("REICAR-%d-%04d", LocalDate.now().getYear(), count);
    }

    public List<ServiceOrder> findByCustomerWithFilters(Customer customer, LocalDate startDate, LocalDate endDate, String sortBy) {
        List<ServiceOrder> orders;

        if (startDate != null && endDate != null) {
            orders = repository.findByCustomerAndDateRange(customer, startDate, endDate);
        } else {
            orders = repository.findByCustomerOrderByEntryDateDesc(customer);
        }

        if ("value".equalsIgnoreCase(sortBy)) {
            orders = orders.stream()
                .sorted(Comparator.comparing(ServiceOrder::getTotalValue).reversed())
                .toList();
        }

        return orders;
    }

    public Optional<ServiceOrder> findByIdForCustomer(Long id, Customer customer) {
        return repository.findByIdWithDetails(id)
            .filter(order -> order.getCustomer().getId().equals(customer.getId()));
    }

    public int getWarrantyDays() {
        return systemConfigRepository.getWarrantyDays();
    }

    @Transactional
    public ServiceOrder claimWarranty(Long orderId, Customer customer, String reason) {
        ServiceOrder order = findByIdForCustomer(orderId, customer)
            .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada"));

        int warrantyDays = getWarrantyDays();
        if (!order.isUnderWarranty(warrantyDays)) {
            throw new IllegalStateException("Serviço fora do período de garantia de " + warrantyDays + " dias");
        }

        if (Boolean.TRUE.equals(order.getWarrantyClaimed())) {
            throw new IllegalStateException("Garantia já foi solicitada para esta ordem de serviço");
        }

        order.setWarrantyClaimed(true);
        order.setWarrantyClaimDate(LocalDate.now());
        order.setWarrantyClaimReason(reason);

        return repository.save(order);
    }
}